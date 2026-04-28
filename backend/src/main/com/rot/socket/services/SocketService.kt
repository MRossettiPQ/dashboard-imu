package com.rot.socket.services

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.Transport
import com.corundumstudio.socketio.protocol.JacksonJsonSupport
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rot.core.config.ApplicationConfig
import com.rot.socket.enums.MessageType
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CustomJacksonJsonSupport : JacksonJsonSupport() {
    init {
        val field = JacksonJsonSupport::class.java.getDeclaredField("objectMapper")
        field.isAccessible = true

        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))

        this.objectMapper.registerModules(javaTimeModule, Jdk8Module())
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)  // Usa o formato ISO-8601
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false)  // "" -> null
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)  // Ignora propriedades desconhecidas
        this.objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val reflectionModule = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .build()
        this.objectMapper.registerModule(reflectionModule)
    }
}

@Singleton
class SocketService(
    private val applicationConfig: ApplicationConfig
) {

    lateinit var server: SocketIOServer

    @Transactional
    fun onStart(@Observes event: StartupEvent) {
        Log.info("Start Socket.IO - $event")

        val config = Configuration()
        config.hostname = applicationConfig.socket().host()
        config.port = applicationConfig.socket().port()
        config.pingInterval = 25000
        config.pingTimeout = 60000
        config.isEnableCors = true
        config.origin = "*"
        config.jsonSupport = CustomJacksonJsonSupport()
        config.setTransports(Transport.WEBSOCKET, Transport.POLLING)
        config.maxFramePayloadLength = 1048576
        config.maxHttpContentLength = 1048576

        server = SocketIOServer(config)
        server.addConnectListener(this::addConnectListener)
        server.addDisconnectListener(this::addDisconnectListener)
        server.start()
        Log.info("Socket.IO Server iniciado na porta ${config.port}")
    }

    fun onStop(@Observes event: ShutdownEvent) {
        Log.info("Stop Socket.IO - ${event.isStandardShutdown}")
        if (::server.isInitialized) server.stop()
    }

    @Transactional
    fun addConnectListener(client: SocketIOClient) {
        val sessionId = client.sessionId
        val remoteAddress = client.remoteAddress
        val handshakeData = client.handshakeData
        var token = handshakeData.httpHeaders["authorization"]
        val typeStr = handshakeData.getSingleUrlParam("type")

        if (typeStr.isNullOrBlank()) {
            Log.warn("Conexão recusada para IP ${client.remoteAddress} - não é sensor e nem usuário")
            return client.disconnect()
        }

        if (token.isNullOrBlank()) {
            token = handshakeData.authToken?.toString()
        }

        if (token.isNullOrBlank()) {
            token = handshakeData.getSingleUrlParam("token")
        }

        // Você pode emitir um evento de boas-vindas, se quiser
        Log.info("Novo cliente conectado! Sessão: $sessionId, IP: $remoteAddress")
        client.sendEvent(MessageType.WELCOME.description, "Bem-vindo! Sua sessão é: $sessionId")
    }

    fun addDisconnectListener(client: SocketIOClient) {
        Log.warn("Cliente desconectado: ${client.sessionId}")
    }

}