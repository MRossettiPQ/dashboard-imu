package com.rot.mqtt.services

import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.mqtt.dto.MqttMessage
import com.rot.session.dtos.CreateMeasurementDto
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.InterceptConnectMessage
import io.moquette.interception.messages.InterceptDisconnectMessage
import io.moquette.interception.messages.InterceptPublishMessage
import io.moquette.interception.messages.InterceptSubscribeMessage
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import java.io.IOException
import java.util.*

@ApplicationScoped
class MqttBrokerService(
    private val applicationConfig: ApplicationConfig
) {

    private lateinit var mqttBroker: Server

    fun start(@Observes event: StartupEvent) {
        try {
            mqttBroker = Server()

            val property = Properties()
            property.setProperty("host", applicationConfig.mqtt().host())
            property.setProperty("port", applicationConfig.mqtt().port().toString())
            mqttBroker.startServer(MemoryConfig(property))
            mqttBroker.addInterceptHandler(MqttInterceptor())
            Log.info("MQTT Broker iniciado na porta ${applicationConfig.mqtt().port()}")
        } catch (e: IOException) {
            Log.error("Falha ao iniciar o broker MQTT - ${e.message}")
        }
    }

    fun stop(@Observes event: ShutdownEvent) {
        if (::mqttBroker.isInitialized) {
            mqttBroker.stopServer()
            Log.info("🛑 MQTT Broker parado")
        }
    }
}

class MqttInterceptor : AbstractInterceptHandler() {
    private fun getBytes(data: InterceptPublishMessage): ByteArray {
        // Obtém o ByteBuf
        val byteBuf = data.payload

        // Cria um array de bytes com o tamanho do ByteBuf
        val bytes = ByteArray(byteBuf.readableBytes())

        // Copia o conteúdo do ByteBuf para o array de bytes
        byteBuf.getBytes(byteBuf.readerIndex(), bytes)
        return bytes
    }

    private inline fun <reified T> convertPayload(data: InterceptPublishMessage): T {
        val bytes: ByteArray = getBytes(data)
        return JsonUtils.toObject<T>(bytes)
    }

    override fun getID(): String {
        return UUID.randomUUID().toString()
    }

    // Chamado quando um cliente se conecta
    override fun onConnect(data: InterceptConnectMessage) {
        Log.info("Cliente conectado: ID=${data.clientID}, Usuário=${data.username}")
        return super.onConnect(data)
    }

    // Chamado quando um cliente publica uma mensagem
    override fun onPublish(data: InterceptPublishMessage) {
        Log.info("Publicação recebida: Tópico=${data.topicName} - Cliente: ${data.username} - clientID: ${data.clientID}")

        val converted = when (data.topicName.lowercase()) {
            "message" -> convertPayload<MqttMessage<String>>(data)
            "join-room" -> convertPayload<MqttMessage<String>>(data)
            "leave-room" -> convertPayload<MqttMessage<String>>(data)
            "command" -> convertPayload<MqttMessage<String>>(data)
            "measurement" -> convertPayload<MqttMessage<MutableList<CreateMeasurementDto>>>(data)
            else -> convertPayload<MqttMessage<String>>(data)
        }

        println(JsonUtils.toJsonString(converted))
        return super.onPublish(data)
    }

    // Chamado quando um cliente assina um tópico
    override fun onSubscribe(data: InterceptSubscribeMessage) {
        Log.info("Assinatura: Cliente=${data.clientID}, Tópico=${data.topicFilter}")
        return super.onSubscribe(data)
    }

    // Chamado quando um cliente se desconecta
    override fun onDisconnect(data: InterceptDisconnectMessage) {
        Log.info("Cliente desconectado: ID=${data.clientID}")
        return super.onDisconnect(data)
    }
}