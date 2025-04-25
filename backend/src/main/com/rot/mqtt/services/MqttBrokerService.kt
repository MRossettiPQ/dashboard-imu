package com.rot.mqtt.services

import com.rot.core.config.ApplicationConfig
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
class MqttBrokerService {

    private lateinit var mqttBroker: Server

    fun startBroker(@Observes event: StartupEvent) {
        try {
            mqttBroker = Server()
//            mqttBroker.addInterceptHandler(MqttInterceptor())

            val property = Properties()
            property.setProperty("host", ApplicationConfig.config.mqtt().host())
            property.setProperty("port", ApplicationConfig.config.mqtt().port().toString())

            mqttBroker.startServer(MemoryConfig(property))
            Log.info("MQTT Broker iniciado na porta ${ApplicationConfig.config.mqtt().port()}")
        } catch (e: IOException) {
            Log.error("Falha ao iniciar o broker MQTT - ${e.message}")
        }
    }

    fun stopBroker(@Observes event: ShutdownEvent) {
        if (::mqttBroker.isInitialized) {
            mqttBroker.stopServer()
            Log.info("🛑 MQTT Broker parado")
        }
    }
}

class MqttInterceptor : AbstractInterceptHandler() {
    override fun getID(): String {
        return UUID.randomUUID().toString()
    }

    // Chamado quando um cliente se conecta
    override fun onConnect(msg: InterceptConnectMessage) {
        Log.infof("🟢 Cliente conectado: ID=${msg.clientID}, Usuário=${msg.username}")
        return super.onConnect(msg)
    }

    // Chamado quando um cliente publica uma mensagem
    override fun onPublish(msg: InterceptPublishMessage) {
        Log.infof("📤 Publicação recebida: Tópico=${msg.topicName}, Payload=${String(msg.payload.array())}")
        return super.onPublish(msg)
    }

    // Chamado quando um cliente assina um tópico
    override fun onSubscribe(msg: InterceptSubscribeMessage) {
        Log.infof("🔔 Assinatura: Cliente=${msg.clientID}, Tópico=${msg.topicFilter}")
        return super.onSubscribe(msg)
    }

    // Chamado quando um cliente se desconecta
    override fun onDisconnect(msg: InterceptDisconnectMessage) {
        Log.infof("🔴 Cliente desconectado: ID=${msg.clientID}")
        return super.onDisconnect(msg)
    }
}