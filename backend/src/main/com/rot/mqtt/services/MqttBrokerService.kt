package com.rot.mqtt.services

import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.core.utils.JwtUtils
import com.rot.measurement.dtos.MeasurementDto
import com.rot.mqtt.dto.MqttMessage
import com.rot.session.dtos.UserSessionContext
import com.rot.session.dtos.SessionSensorDto
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.moquette.broker.security.IAuthenticator
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.InterceptPublishMessage
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttQoS
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class MqttBrokerService(
    private val applicationConfig: ApplicationConfig
) {
    private lateinit var mqttBroker: Server

    // O seu cache em memória seguro para concorrência
    val activeSessions: ConcurrentHashMap<UUID, UserSessionContext> = ConcurrentHashMap()

    fun start(@Observes event: StartupEvent) {
        try {
            mqttBroker = Server()

            val property = Properties()
            property.setProperty("host", applicationConfig.mqtt().host())
            property.setProperty("port", applicationConfig.mqtt().port().toString())
            property.setProperty("websocket_port", applicationConfig.mqtt().socketPort().toString())
            property.setProperty("allow_anonymous", "true")

            val authenticator = JwtMqttAuthenticator()
            mqttBroker.startServer(
                MemoryConfig(property),
                emptyList(),
                null,
                authenticator,
                null
            )

            // Passamos a referência do Service para o Interceptor poder acessar o cache
            mqttBroker.addInterceptHandler(MqttInterceptor(this))

            Log.info("MQTT Broker TCP na porta ${applicationConfig.mqtt().port()} e WS na porta ${applicationConfig.mqtt().socketPort()}")
        } catch (e: Exception) {
            Log.error("Falha ao iniciar o broker MQTT - ${e.message}")
        }
    }

    fun stop(@Observes event: ShutdownEvent) {
        if (::mqttBroker.isInitialized) {
            mqttBroker.stopServer()
            Log.info("🛑 MQTT Broker parado")
        }
    }

    fun sendCommandToSensor(macAddress: String, command: String) {
        val payload = JsonUtils.toJsonByteArray(mapOf("command" to command))

        val message = MqttMessageBuilders.publish()
            .topicName("sensor/$macAddress/command")
            .retained(false)
            .qos(MqttQoS.AT_MOST_ONCE)
            .payload(Unpooled.copiedBuffer(payload))
            .build()

        mqttBroker.internalPublish(message, "backend-system")
    }
}

class JwtMqttAuthenticator : IAuthenticator {
    override fun checkValid(clientId: String?, username: String?, password: ByteArray?): Boolean {
        if (password == null) {
            Log.warn("Tentativa de conexão MQTT recusada: Senha/Token ausente (ClientID: $clientId)")
            return false
        }

        // Converte os bytes da senha para String e limpa o "Bearer " se existir
        val token = String(password).replace("Bearer ", "")

        return try {
            // Usa o seu JwtUtils já existente para decodificar e validar
            val decoded = JwtUtils.decode(token)

            if (decoded != null) {
                Log.debug("Conexão MQTT autorizada para: ${decoded.name}")
                true
            } else {
                Log.warn("Tentativa de conexão MQTT recusada: Token inválido")
                false
            }
        } catch (e: Exception) {
            Log.error("Erro ao processar JWT no MQTT", e)
            false
        }
    }
}

class MqttInterceptor(
    private val brokerService: MqttBrokerService // Referência ao serviço principal
) : AbstractInterceptHandler() {

    override fun getID(): String = UUID.randomUUID().toString()

    override fun onPublish(data: InterceptPublishMessage) {
        val topic = data.topicName

        try {
            router(topic, data)
        } catch (e: Exception) {
            Log.error("Erro ao processar mensagem MQTT no tópico $topic", e)
        }

        super.onPublish(data) // Importante manter para o Moquette rotear a mensagem para o frontend
    }

    fun router(topic: String, data: InterceptPublishMessage) {
        when {
            // 1. Registro de um novo sensor ao ligar
            topic == "sensor/register" -> {
                val sensorDto = convertPayload<SessionSensorDto>(data)
                Log.info("Sensor registrado: ${sensorDto.sensorInfo?.macAddress}")
                // Aqui você salvaria no banco/lista de sensores disponíveis
            }

            // 2. Recebimento de medições roteadas por Sessão
            topic.startsWith("session/") && topic.endsWith("/measurement") -> {
                // Extrai o ID da sessão da string do tópico (ex: "session/123-abc/measurement")
                val sessionIdStr = topic.split("/")[1]
                val sessionId = UUID.fromString(sessionIdStr)

                val measurements = convertPayload<MqttMessage<MutableList<MeasurementDto>>>(data)

                // Salva no cache em memória do servidor
                val sessionContext = brokerService.activeSessions[sessionId]
                if (sessionContext != null) {
                    // Lógica para encontrar o sensor no contexto e dar .addAll(measurements)
                    // ...

                    // Lógica de Flush: Se cache passar de 1000 itens, salva no DB em Batch.
                }
            }
        }
    }

    private inline fun <reified T> convertPayload(data: InterceptPublishMessage): T {
        val byteBuf = data.payload
        val bytes = ByteArray(byteBuf.readableBytes())
        byteBuf.getBytes(byteBuf.readerIndex(), bytes)
        return JsonUtils.toObject<T>(bytes)
    }
}