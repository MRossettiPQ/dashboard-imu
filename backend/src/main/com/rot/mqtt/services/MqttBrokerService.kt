package com.rot.mqtt.services

import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.core.utils.JwtUtils
import com.rot.measurement.models.SensorInfo
import com.rot.session.services.MeasurementPersistenceService
import com.rot.mqtt.dto.MeasurementBatchPayload
import com.rot.mqtt.dto.SensorRegisterPayload
import com.rot.mqtt.dto.SensorStatusPayload
import com.rot.session.dtos.SensorSessionContext
import com.rot.session.dtos.UserSessionContext
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.moquette.broker.security.IAuthenticator
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.*
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttQoS
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class MqttBrokerService(
    private val applicationConfig: ApplicationConfig,
    val measurementPersistenceService: MeasurementPersistenceService
) {
    private lateinit var mqttBroker: Server

    // Sensores conectados ao broker, indexados por MAC address.
    val connectedSensors: ConcurrentHashMap<String, SensorSessionContext> = ConcurrentHashMap()

    // Sessões ativas do usuário, indexadas pelo UUID da sessão.
    val activeSessions: ConcurrentHashMap<UUID, UserSessionContext> = ConcurrentHashMap()

    /**
     * Mapa reverso: clientId do Moquette → MAC address do sensor.
     * Necessário porque onDisconnect/onConnectionLost só recebem clientId,
     * mas nosso modelo é indexado por MAC.
     * Populado no handleSensorRegister quando o sensor publica sensor/{mac}/register.
     */
    val clientIdToMac: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    /**
     * Mapa reverso: clientId do Moquette → UUID do usuário autenticado.
     * Populado no onConnect quando o frontend conecta com JWT.
     */
    val clientIdToUserId: ConcurrentHashMap<String, UUID> = ConcurrentHashMap()

    @Transactional
    fun start(@Observes event: StartupEvent) {
        try {
            SensorInfo.update("active = false")


            mqttBroker = Server()

            val property = Properties()
            property.setProperty("host", applicationConfig.mqtt().host())
            property.setProperty("port", applicationConfig.mqtt().port().toString())
            property.setProperty("websocket_port", applicationConfig.mqtt().socketPort().toString())
            property.setProperty("allow_anonymous", "true")

            val authenticator = JwtMqttAuthenticator(this)
            mqttBroker.startServer(
                MemoryConfig(property),
                emptyList(),
                null,
                authenticator,
                null
            )

            mqttBroker.addInterceptHandler(MqttInterceptor(this))

            Log.info("MQTT Broker TCP na porta ${applicationConfig.mqtt().port()} e WS na porta ${applicationConfig.mqtt().socketPort()}")
        } catch (e: Exception) {
            Log.error("Falha ao iniciar o broker MQTT - ${e.message}", e)
        }
    }

    fun stop(@Observes event: ShutdownEvent) {
        if (::mqttBroker.isInitialized) {
            measurementPersistenceService.flushAll()
            mqttBroker.stopServer()
            Log.info("MQTT Broker parado")
        }
    }

    // ─── Publicação de mensagens ────────────────────────────────────────

    fun publish(topic: String, payload: Any, qos: MqttQoS = MqttQoS.AT_MOST_ONCE) {
        val bytes = JsonUtils.toJsonByteArray(payload)
        val message = MqttMessageBuilders.publish()
            .topicName(topic)
            .retained(false)
            .qos(qos)
            .payload(Unpooled.copiedBuffer(bytes))
            .build()
        mqttBroker.internalPublish(message, "backend-system")
    }

    fun sendCommandToSensor(macAddress: String, command: String, sessionId: UUID? = null) {
        val payload = mutableMapOf<String, Any?>("command" to command)
        if (sessionId != null) {
            payload["sessionId"] = sessionId.toString()
        }
        publish("sensor/$macAddress/command", payload)
        Log.info("Comando '$command' enviado para sensor $macAddress")
    }

    fun sendCommandToSession(sessionId: UUID, command: String) {
        publish(
            topic = "session/$sessionId/command",
            payload = mapOf(
                "command" to command,
                "sessionId" to sessionId.toString()
            )
        )
        Log.info("Comando de broadcast '$command' enviado para o canal da sessão $sessionId")
    }

    fun publishSessionStatus(sessionId: UUID, status: String) {
        publish(
            "session/$sessionId/status",
            mapOf("status" to status, "sessionId" to sessionId.toString())
        )
    }

    fun publishAvailableSensors() {
        val available = connectedSensors.values
            .filter { it.available }
            .map { mapOf("mac" to it.mac, "name" to it.name, "ip" to it.ip) }
        publish("sensors/available", mapOf("sensors" to available))
    }

    // ─── Gerenciamento de sensores ──────────────────────────────────────

    fun assignSensorToSession(macAddress: String, sessionId: UUID): Boolean {
        val sensor = connectedSensors[macAddress]
        if (sensor == null) {
            Log.warn("Sensor $macAddress não encontrado")
            return false
        }
        if (!sensor.available) {
            Log.warn("Sensor $macAddress já está em uso na sessão ${sensor.sessionId}")
            return false
        }

        sensor.available = false
        sensor.sessionId = sessionId

        val session = activeSessions[sessionId]
        session?.assignedSensors?.add(macAddress)

        sendCommandToSensor(macAddress, "ASSIGN", sessionId)
        publishAvailableSensors()
        Log.info("Sensor $macAddress atribuído à sessão $sessionId")
        return true
    }

    fun releaseSensorFromSession(macAddress: String): Boolean {
        val sensor = connectedSensors[macAddress] ?: return false

        val sessionId = sensor.sessionId
        sensor.available = true
        sensor.sessionId = null

        if (sessionId != null) {
            activeSessions[sessionId]?.assignedSensors?.remove(macAddress)
        }

        sendCommandToSensor(macAddress, "RELEASE")
        publishAvailableSensors()
        Log.info("Sensor $macAddress liberado da sessão $sessionId")
        return true
    }

    /**
     * Limpeza completa quando um sensor desconecta (chamado pelo interceptor).
     * Faz flush de medições pendentes, libera da sessão e remove dos mapas.
     */

    // ATUALIZADO: Quando sensor desconectar deve parar sessão
    fun handleSensorDisconnect(clientId: String) {
        val mac = clientIdToMac.remove(clientId) ?: return
        val sensor = connectedSensors.remove(mac) ?: return

        if (sensor.sessionId != null) {
            val sessionId = sensor.sessionId!!
            measurementPersistenceService.flush("$sessionId:$mac")

            // NOVO: Faz o broadcast de STOP para a sessão inteira porque um hardware caiu
            Log.warn("Sensor $mac caiu. Parando a sessão inteira $sessionId via broadcast.")
            sendCommandToSession(sessionId, "STOP")
            publishSessionStatus(sessionId, "STOPPED_BY_ERROR")

            activeSessions[sessionId]?.assignedSensors?.remove(mac)
        }

        measurementPersistenceService.deactivateSensor(mac)
        publishAvailableSensors() // Este é o canal que o frontend usa para receber a lista atualizada
    }

    /**
     * Limpeza quando um frontend/usuário desconecta.
     */
    fun handleUserDisconnect(clientId: String) {
        val userId = clientIdToUserId.remove(clientId) ?: return
        Log.info("Usuário $userId (clientId: $clientId) desconectado")
        // Sessões permanecem ativas - o usuário pode reconectar.
        // Se quiser auto-parar, descomente:
        // activeSessions.values
        //     .filter { it.userId == userId }
        //     .forEach { sendCommandToSession(it.id!!, "STOP") }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Autenticador JWT
// ═══════════════════════════════════════════════════════════════════════════

class JwtMqttAuthenticator(
    private val broker: MqttBrokerService
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
                        broker.clientIdToUserId[clientId] = UUID.fromString(userId)
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

// ═══════════════════════════════════════════════════════════════════════════
// Interceptor MQTT - Todos os lifecycle callbacks
// ═══════════════════════════════════════════════════════════════════════════

class MqttInterceptor(
    private val broker: MqttBrokerService
) : AbstractInterceptHandler() {

    override fun getID(): String = "mqtt-interceptor-main"

    // ─── onConnect ──────────────────────────────────────────────────────
    // Chamado quando qualquer client (sensor ou frontend) se conecta ao broker.
    // Neste ponto o client ainda não publicou nada, então não sabemos se é
    // sensor ou frontend. A identificação acontece depois:
    //   - Sensor: quando publica em sensor/{mac}/register
    //   - Frontend: já mapeado no JwtMqttAuthenticator via JWT
    override fun onConnect(message: InterceptConnectMessage) {
        val clientId = message.clientID
        Log.info("MQTT Client conectado: $clientId (username: ${message.username})")
    }

    // ─── onDisconnect ───────────────────────────────────────────────────
    // Chamado quando o client envia um DISCONNECT limpo (graceful).
    // O sensor/frontend está saindo normalmente.
    override fun onDisconnect(message: InterceptDisconnectMessage) {
        val clientId = message.clientID
        Log.info("MQTT Client desconectou (graceful): $clientId")
        handleClientGone(clientId)
    }

    // ─── onConnectionLost ───────────────────────────────────────────────
    // Chamado quando a conexão é perdida abruptamente (timeout, rede caiu,
    // sensor desligou sem enviar DISCONNECT). Este é o caso mais comum
    // para sensores ESP32 que são desligados fisicamente.
    override fun onConnectionLost(message: InterceptConnectionLostMessage) {
        val clientId = message.clientID
        Log.warn("MQTT Client perdeu conexão (abrupto): $clientId")
        handleClientGone(clientId)
    }

    // ─── onSubscribe ────────────────────────────────────────────────────
    // Chamado quando um client se inscreve em um tópico.
    // Útil para log/debug e para saber quais frontends estão ouvindo sessões.
    override fun onSubscribe(message: InterceptSubscribeMessage) {
        val clientId = message.clientID
        val topic = message.topicFilter
        Log.info("MQTT Client $clientId inscreveu-se em: $topic")

        // Se um frontend se inscreveu em session/{id}/#, podemos rastrear
        // quais sessões estão sendo monitoradas ativamente
    }

    // ─── onUnsubscribe ──────────────────────────────────────────────────
    // Chamado quando um client cancela inscrição em um tópico.
    override fun onUnsubscribe(message: InterceptUnsubscribeMessage) {
        val clientId = message.clientID
        val topic = message.topicFilter
        Log.info("MQTT Client $clientId desinscreveu-se de: $topic")
    }

    // ─── onMessageAcknowledged ──────────────────────────────────────────
    // Chamado quando uma mensagem QoS 1+ é confirmada pelo client.
    // Não usamos QoS 1/2 nas medições (são QoS 0 para performance),
    // mas pode ser útil para comandos críticos no futuro.
    override fun onMessageAcknowledged(message: InterceptAcknowledgedMessage) {
        Log.debug("MQTT Mensagem ACK recebido: topic=${message.topic}, clientId=${message.username}")
    }

    // ─── onPublish ──────────────────────────────────────────────────────
    // Chamado quando qualquer client publica uma mensagem.
    // Este é o roteador principal de lógica de negócio.
    override fun onPublish(data: InterceptPublishMessage) {
        val topic = data.topicName

        try {
            route(topic, data)
        } catch (e: Exception) {
            Log.error("Erro ao processar mensagem MQTT no tópico '$topic'", e)
        }

        // Deixa o Moquette rotear a mensagem para outros subscribers (frontend)
        super.onPublish(data)
    }

    // ─── Roteamento de mensagens ────────────────────────────────────────

    private fun route(topic: String, data: InterceptPublishMessage) {
        when {
            // sensor/{mac}/register
            topic.matches(Regex("^sensor/([^/]+)/register$")) -> {
                handleSensorRegister(topic, data)
            }
            // session/{uuid}/measurement
            topic.matches(Regex("^session/([^/]+)/measurement$")) -> {
                handleMeasurement(topic, data)
            }
            // sensor/{mac}/status
            topic.matches(Regex("^sensor/([^/]+)/status$")) -> {
                handleSensorStatus(topic, data)
            }
        }
    }

    /**
     * Lógica comum para desconexão (graceful ou abrupta).
     * Verifica se é sensor ou frontend e faz a limpeza apropriada.
     */
    private fun handleClientGone(clientId: String) {
        // Tenta como sensor primeiro (mais comum desconectar)
        if (broker.clientIdToMac.containsKey(clientId)) {
            broker.handleSensorDisconnect(clientId)
            return
        }

        // Tenta como frontend/usuário
        if (broker.clientIdToUserId.containsKey(clientId)) {
            broker.handleUserDisconnect(clientId)
            return
        }

        Log.debug("Client $clientId desconectado mas não era rastreado (possivelmente nunca se registrou)")
    }

    // ─── Handlers de mensagens ──────────────────────────────────────────

    private fun handleSensorRegister(topic: String, data: InterceptPublishMessage) {
        val payload = extractPayload<SensorRegisterPayload>(data)
        val mac = payload.mac ?: return Log.warn("Registro de sensor sem MAC address")
        val clientId = data.clientID

        // Mapeia clientId → MAC para rastrear desconexão
        if (clientId != null) {
            broker.clientIdToMac[clientId] = mac
        }

        val context = SensorSessionContext().apply {
            this.mac = mac
            this.name = payload.name
            this.ip = payload.ip
            this.available = true
            this.sessionId = null
            this.clientId = clientId
        }

        broker.connectedSensors[mac] = context
        Log.info("Sensor registrado: $mac (IP: ${payload.ip}, Nome: ${payload.name}, ClientID: $clientId)")

        broker.measurementPersistenceService.registerSensor(mac, payload.name, payload.ip)
        broker.publishAvailableSensors()
    }

    private fun handleMeasurement(topic: String, data: InterceptPublishMessage) {
        val sessionIdStr = topic.split("/")[1]
        val sessionId: UUID
        try {
            sessionId = UUID.fromString(sessionIdStr)
        } catch (e: Exception) {
            return Log.warn("Session ID inválido no tópico: $sessionIdStr")
        }

        val payload = extractPayload<MeasurementBatchPayload>(data)
        val measurements = payload.content
        if (measurements.isNullOrEmpty()) return

        val mac = payload.originIdentifier ?: return Log.warn("Medição sem identificador de origem")

        Log.debug("Recebidas ${measurements.size} medições do sensor $mac para sessão $sessionId")
        broker.measurementPersistenceService.buffer(sessionId, mac, measurements)
    }

    private fun handleSensorStatus(topic: String, data: InterceptPublishMessage) {
        val mac = topic.split("/")[1]
        val payload = extractPayload<SensorStatusPayload>(data)

        val sensor = broker.connectedSensors[mac]
        if (sensor != null) {
            when (payload.status) {
                "AVAILABLE" -> {
                    if (sensor.sessionId == null) {
                        sensor.available = true
                    }
                    Log.info("Sensor $mac reportou status: AVAILABLE")
                }
                "CALIBRATING" -> {
                    sensor.available = false
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