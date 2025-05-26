package com.rot.socket.services

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import com.fasterxml.jackson.databind.JsonNode
import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JwtUtils
import com.rot.socket.dtos.MessageRegisterSensorDto
import com.rot.socket.dtos.MessageRegisterUserDto
import com.rot.socket.dtos.MessageSaveSessionDto
import com.rot.socket.enums.SocketEvents
import com.rot.user.models.User
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Singleton
import jakarta.transaction.Transactional


@Singleton
class SocketService(
    private val applicationConfig: ApplicationConfig
) {
    lateinit var server: SocketIOServer

    fun onStart(@Observes event: StartupEvent) {
        val config = Configuration()
        config.hostname = applicationConfig.socket().host()
        config.port = applicationConfig.socket().port()
        config.pingInterval = 25000
        config.pingTimeout = 60000

        server = SocketIOServer(config)
        server.addConnectListener(this::addConnectListener)
        server.addDisconnectListener(this::addDisconnectListener)
        server.addEventListener(SocketEvents.STOP, JsonNode::class.java, this::addStopListener)
        server.addEventListener(SocketEvents.START, JsonNode::class.java, this::addStartListener)
        server.addEventListener(SocketEvents.RESTART, JsonNode::class.java, this::addRestartListener)
        server.addEventListener(SocketEvents.JOIN_ROOM, String::class.java, this::addJoinRoomListener)
        server.addEventListener(SocketEvents.LEAVE_ROOM, String::class.java, this::addLeaveRoomListener)
        server.addEventListener(SocketEvents.MEASUREMENT, JsonNode::class.java, this::addMeasurementListener)
        server.addEventListener(SocketEvents.SAVE_SESSION, MessageSaveSessionDto::class.java, this::addSaveSessionListener)
        server.addEventListener(SocketEvents.REGISTER_USER, MessageRegisterUserDto::class.java, this::addRegisterUserListener)
        server.addEventListener(SocketEvents.REGISTER_SENSOR, MessageRegisterSensorDto::class.java, this::addRegisterSensorListener)

        server.start()
        Log.info("Socket.IO Server iniciado na porta ${config.port}")
    }

    fun onStop(@Observes event: ShutdownEvent) {
        Log.info("Stop Socket.IO")
        if (::server.isInitialized) server.stop()
    }

    @Transactional
    fun addConnectListener(client: SocketIOClient) {
        val sessionId = client.sessionId
        val remoteAddress = client.remoteAddress
        val token = client.handshakeData.getSingleUrlParam("token")

        val decoded = JwtUtils.decode(token)
        if (token == null || decoded == null || decoded.issuer != applicationConfig.security().issuer()) {
            Log.warn("Conexão recusada para IP ${client.remoteAddress}")
            return client.disconnect()
        }

        val user = User.createQuery().fetchFirst()
        println("Novo cliente conectado! Sessão: $sessionId, IP: $remoteAddress")
        println(user)

        // Você pode emitir um evento de boas-vindas, se quiser
        client.sendEvent("welcome", "Bem-vindo! Sua sessão é: $sessionId")
    }

    fun addDisconnectListener(client: SocketIOClient) {
        val sessionId = client.sessionId
        println("Cliente desconectado: $sessionId")

        // Por exemplo, remover o cliente de uma sala se necessário
        // client.leaveRoom("algumaSala")
    }

    fun addLeaveRoomListener(client: SocketIOClient, message: String, request: AckRequest) {
        client.leaveRoom(message)
        Log.info("Cliente ${client.sessionId} saiu da sala $message")
        client.sendEvent("room_left", "Você saiu da sala: $message")
    }

    fun addJoinRoomListener(client: SocketIOClient, message: String, request: AckRequest) {
        client.joinRoom(message)
        Log.info("Cliente ${client.sessionId} entrou na sala $message")
        client.sendEvent("room_joined", "Você entrou na sala: $message")
    }

    fun addMeasurementListener(client: SocketIOClient, message: JsonNode, request: AckRequest) {
        Log.info("Register measurement listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addStartListener(client: SocketIOClient, message: JsonNode, request: AckRequest) {
        Log.info("Register start listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addStopListener(client: SocketIOClient, message: JsonNode, request: AckRequest) {
        Log.info("Register stop listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addRestartListener(client: SocketIOClient, message: JsonNode, request: AckRequest) {
        Log.info("Register restart listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addRegisterUserListener(client: SocketIOClient, message: MessageRegisterUserDto, request: AckRequest) {
        Log.info("Register user listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addRegisterSensorListener(client: SocketIOClient, message: MessageRegisterSensorDto, request: AckRequest) {
        Log.info("Register sensor listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }

    fun addSaveSessionListener(client: SocketIOClient, message: MessageSaveSessionDto, request: AckRequest) {
        Log.info("Register sensor listener: $message")
        client.sendEvent("chat", "Servidor recebeu: $message")

        request.sendAckData("Aceitei viu")
    }
}