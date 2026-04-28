package com.rot.mqtt.services

import com.rot.core.utils.JsonUtils
import com.rot.core.utils.JwtUtils
import com.rot.mqtt.dto.MeasurementBatchPayload
import com.rot.mqtt.dto.SensorRegisterPayload
import com.rot.mqtt.dto.SensorStatusPayload
import com.rot.session.dtos.ClientSessionContext
import com.rot.session.dtos.SensorSessionContext
import com.rot.session.dtos.UserSessionContext
import com.rot.session.enums.SessionContextType
import com.rot.session.services.SessionService
import io.moquette.broker.security.IAuthenticator
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.*
import io.quarkus.logging.Log
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class JwtMqttAuthenticator(
    private val broker: SessionService
) : IAuthenticator {

    override fun checkValid(clientId: String?, username: String?, password: ByteArray?): Boolean {
        if (username?.startsWith("MPU") == true &&  (password == null || password.isEmpty())) {
            // Sensor ESP32 conecta sem credenciais - identificação via tópico de registro
            Log.debug("Conexão MQTT sem credenciais (sensor?) - ClientID: $clientId")
            return true
        }

        if (password == null || password.isEmpty()) {
            return false
        }

        val token = String(password).replace("Bearer ", "")
        return try {
            val decoded = JwtUtils.decode(token)
            if (decoded != null) {
                // Mapeia o clientId ao userId para rastrear desconexão
                if (clientId != null) {
                    val userId = decoded.getClaim<String>("reference")
                    if (userId != null) {
                        val userUuid = UUID.fromString(userId)
                        var context = broker.getContextByKey(userId)

                        if (context == null) {
                            context = ClientSessionContext().apply {
                                this.sessionId = null
                                this.clientId = clientId
                                this.type = SessionContextType.USER
                                this.user = UserSessionContext().apply {
                                    this.userId = userUuid
                                    this.patientId = null
                                    this.assignedSensors = CopyOnWriteArraySet()
                                }
                            }
                        }

                        context.user!!.userId = userUuid
                        context.clientId = clientId
                        context.type = SessionContextType.USER

                        broker.contexts[userId] = context
                    }
                }
                Log.debug("Conexão MQTT autorizada: ${decoded.name} (clientId: $clientId)")
                true
            } else {
                Log.warn("Conexão MQTT recusada: Token inválido (clientId: $clientId)")
                false
            }
        } catch (e: Exception) {
            Log.error("Erro ao processar JWT no MQTT (clientId: $clientId)", e)
            false
        }
    }
}

class MqttInterceptor(
    private val broker: SessionService
) : AbstractInterceptHandler() {

    override fun getID(): String = "mqtt-interceptor-main"

    override fun onConnect(message: InterceptConnectMessage) {
        // Chamado quando qualquer client (sensor ou frontend) se conecta ao broker.
        // Neste ponto o client ainda não publicou nada, então não sabemos se é
        // sensor ou frontend. A identificação acontece depois:
        //   - Sensor: quando publica em sensor/{mac}/register
        //   - Frontend: já mapeado no JwtMqttAuthenticator via JWT
        val clientId = message.clientID
        Log.info("MQTT Client conectado: $clientId (username: ${message.username})")
    }

    override fun onDisconnect(message: InterceptDisconnectMessage) {
        // Chamado quando o client envia um DISCONNECT limpo (graceful).
        // O sensor/frontend está saindo normalmente.
        val clientId = message.clientID
        Log.info("MQTT Client desconectou (graceful): $clientId")
        handleClientGone(clientId)
    }

    override fun onConnectionLost(message: InterceptConnectionLostMessage) {
        // Chamado quando a conexão é perdida abruptamente (timeout, rede caiu,
        // sensor desligou sem enviar DISCONNECT). Este é o caso mais comum
        // para sensores ESP32 que são desligados fisicamente.
        val clientId = message.clientID
        Log.warn("MQTT Client perdeu conexão (abrupto): $clientId")
        handleClientGone(clientId)
    }

    override fun onSubscribe(message: InterceptSubscribeMessage) {
        // Chamado quando um client se inscreve em um tópico.
        val clientId = message.clientID
        val topic = message.topicFilter
        Log.info("MQTT Client $clientId inscreveu-se em: $topic")
    }

    override fun onUnsubscribe(message: InterceptUnsubscribeMessage) {
        // Chamado quando um client cancela inscrição em um tópico.
        val clientId = message.clientID
        val topic = message.topicFilter
        Log.info("MQTT Client $clientId desinscreveu-se de: $topic")
    }

    override fun onPublish(data: InterceptPublishMessage) {
        // Chamado quando qualquer client publica uma mensagem.
        // Este é o roteador principal de lógica de negócio.
        val topic = data.topicName

        try {
            route(topic, data)
        } catch (e: Exception) {
            Log.error("Erro ao processar mensagem MQTT no tópico '$topic'", e)
        }

        super.onPublish(data)
    }

    private fun route(topic: String, data: InterceptPublishMessage) {
        when {
            // sensor/{mac}/register
            topic.matches(Regex("^sensor/([^/]+)/register$")) -> {
                handleSensorRegister(data)
            }
            // session/{uuid}/measurement
            topic.matches(Regex("^session/([^/]+)/sensor/([^/]+)/measurements$")) -> {
                handleMeasurement(topic, data)
            }
            // sensor/{mac}/status
            topic.matches(Regex("^sensor/([^/]+)/status$")) -> {
                handleSensorStatus(topic, data)
            }
        }
    }

    private fun handleClientGone(clientId: String) {
        val entry = broker.contexts.entries.find { it.value.clientId == clientId } ?: run {
            Log.debug("Client $clientId desconectado mas não rastreado")
            return
        }

        when (entry.value.type) {
            SessionContextType.SENSOR -> broker.handleSensorDisconnect(entry.key)
            SessionContextType.USER -> broker.handleUserDisconnect(entry.key)
        }
    }

    private fun handleSensorRegister(data: InterceptPublishMessage) {
        val payload = extractPayload<SensorRegisterPayload>(data)
        val mac = payload.mac
            ?: return Log.warn("Registro de sensor sem MAC address")
        val clientId = data.clientID

        val sensorInfo = broker.registerSensor(mac, payload.name)
        broker.contexts[mac] = ClientSessionContext().apply {
            this.sessionId = null
            this.clientId = clientId
            this.type = SessionContextType.SENSOR
            this.sensor = SensorSessionContext().apply {
                this.id = sensorInfo.id
                this.mac = sensorInfo.macAddress
                this.name = sensorInfo.sensorName
                this.ip = payload.ip
                this.available = true
            }
        }

        Log.info("Sensor registrado: $mac (IP: ${payload.ip})")
        broker.publishAvailableSensors()
    }

    private fun handleMeasurement(topic: String, data: InterceptPublishMessage) {
        val parts = topic.split("/")
        val sessionId = UUID.fromString(parts[1])
        val mac = parts[3]

        val payload = extractPayload<MeasurementBatchPayload>(data)
        val measurements = payload.content
        if (measurements.isNullOrEmpty()) return

        Log.debug("Recebidas ${measurements.size} medições do sensor $mac para sessão $sessionId")
        broker.buffer(sessionId, mac, measurements)
    }

    private fun handleSensorStatus(topic: String, data: InterceptPublishMessage) {
        val mac = topic.split("/")[1]
        val payload = extractPayload<SensorStatusPayload>(data)

        val sensor = broker.getContextByKey(mac)
        if (sensor != null) {
            when (payload.status) {
                "AVAILABLE" -> {
                    if (sensor.sessionId == null) {
                        sensor.setAvailable(true)
                        Log.info("Sensor $mac reportou status: AVAILABLE")
                    }
                }
                "CALIBRATING" -> {
                    sensor.setAvailable(false)
                    Log.info("Sensor $mac entrou em calibração")
                }
                "ERROR" -> {
                    Log.error("Sensor $mac reportou erro: ${payload.message}")
                }
            }
            broker.publishAvailableSensors()
        }
    }

    private inline fun <reified T> extractPayload(data: InterceptPublishMessage): T {
        val byteBuf = data.payload
        val bytes = ByteArray(byteBuf.readableBytes())
        byteBuf.getBytes(byteBuf.readerIndex(), bytes)
        return JsonUtils.toObject<T>(bytes)
    }
}