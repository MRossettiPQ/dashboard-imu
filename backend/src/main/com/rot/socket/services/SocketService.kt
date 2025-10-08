package com.rot.socket.services

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.protocol.JacksonJsonSupport
import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.core.utils.JwtUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.*
import com.rot.socket.dtos.*
import com.rot.socket.enums.MessageType
import com.rot.socket.enums.UserSessionType
import com.rot.user.models.User
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.util.*

class CustomJacksonJsonSupport : JacksonJsonSupport() {
    init {
        val field = JacksonJsonSupport::class.java.getDeclaredField("objectMapper")
        field.isAccessible = true
        field[this] = JsonUtils.MAPPER
    }
}

@Singleton
class SocketService(
    private val applicationConfig: ApplicationConfig
) {
    val sessions: MutableMap<UUID, SessionContext> = mutableMapOf()
    val sensors: MutableList<SessionSensorDto> = mutableListOf()

    lateinit var server: SocketIOServer

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

        server = SocketIOServer(config)
        server.addConnectListener(this::addConnectListener)
        server.addDisconnectListener(this::addDisconnectListener)

        // Client -> Server
        server.addEventListener(MessageType.CLIENT_SERVER_STOP.description, String::class.java, this::clientServerCommandStop)
        server.addEventListener(MessageType.CLIENT_SERVER_START.description, String::class.java, this::clientServerCommandStart)
        server.addEventListener(MessageType.CLIENT_SERVER_SENSOR_LIST.description, String::class.java, this::clientServerSensorList)
        server.addEventListener(MessageType.CLIENT_SERVER_ADD_SENSOR.description, MessageAddSensorDto::class.java, this::clientServerAddSensor)
        server.addEventListener(MessageType.CLIENT_SERVER_REMOVE_SENSOR.description, MessageRemoveSensorDto::class.java, this::clientServerRemoveSensor)
        server.addEventListener(MessageType.CLIENT_SERVER_SAVE_SESSION.description, MessageSaveSessionDto::class.java, this::clientServerSaveSession)

        // Sensor -> Server
        server.addEventListener(MessageType.SENSOR_SERVER_MEASUREMENT.description, MessageSensorMeasurementBlock::class.java, this::sensorServerMeasurement)
        server.addEventListener(MessageType.SENSOR_SERVER_REGISTER_SENSOR.description, MessageSessionSensorDto::class.java, this::sensorServerRegisterSensor)

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
            Log.warn("Conexão recusada para IP ${client.remoteAddress} - não é sensor e nem usúario")
            return client.disconnect()
        }

        if (token.isNullOrBlank()) {
            token = handshakeData.authToken?.toString()
        }

        if (token.isNullOrBlank()) {
            token = handshakeData.getSingleUrlParam("token")
        }

        val type = UserSessionType.valueOf(typeStr)
        val sessionContext = SessionContext()
        if (type == UserSessionType.USER && !token.isNullOrBlank()) {
            val decoded = JwtUtils.decode(token.replace("Bearer ", ""))
            if (decoded == null || decoded.issuer != applicationConfig.security().issuer()) {
                Log.warn("Conexão recusada para IP ${client.remoteAddress}")
                return client.disconnect()
            }

            val user = User.createQuery()
                .where(User.q.id.eq(UUID.fromString(decoded.getClaim("reference"))))
                .where(User.q.username.eq(decoded.name))
                .fetchFirst()

            if (user == null) {
                Log.warn("Usuário não encontrado para IP ${client.remoteAddress} - ${decoded.name}")
                return client.disconnect()
            }

            sessionContext.room = sessionId
            sessionContext.userId = user.id
            sessionContext.type = UserSessionType.USER

            client.joinRoom(sessionId.toString())

            sendSensorList(client)
        } else if (type == UserSessionType.SENSOR) {
            sessionContext.type = UserSessionType.SENSOR

            broadcastSensorList()
        } else {
            Log.warn("Conexão recusada para IP ${client.remoteAddress} - não é sensor e nem usúario")
            return client.disconnect()
        }
        sessions.put(sessionId, sessionContext)

        // Você pode emitir um evento de boas-vindas, se quiser
        Log.info("Novo cliente conectado! Sessão: $sessionId, IP: $remoteAddress")
        client.sendEvent(MessageType.WELCOME.description, "Bem-vindo! Sua sessão é: $sessionId")
    }

    fun addDisconnectListener(client: SocketIOClient) {
        Log.warn("Cliente desconectado: ${client.sessionId}")
        val sessionId = client.sessionId
        val sessionContext = sessions[sessionId]
        if (sessionContext == null) return

        if (sessionContext.type == UserSessionType.USER) {
            // Remover sensores da sala desse usuário
            val roomId = sessionContext.room.toString()
            val rom = server.getRoomOperations(roomId)
            rom.clients.forEach { sensorClient ->
                runCatching {
                    sensorClient.leaveRoom(roomId)
                }
            }
        }

        if (sessionContext.type == UserSessionType.SENSOR) {
            removeSensor(sessionContext.sensorId)
        }

        if (sessionContext.room != null) {
            if (sessionContext.type == UserSessionType.SENSOR) {
                runCatching {
                    val rom = server.getRoomOperations(sessionContext.room.toString())
                    rom?.sendEvent(MessageType.SERVER_SENSOR_REMOVED_ROOM.description, "Sensor ${sessionContext.sensorId} saiu da sala.")
                }
            }

            client.leaveRoom(sessionContext.room.toString())
        }

        sessions.remove(sessionId)
    }

    // Client -> Server
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    fun clientServerSaveSession(client: SocketIOClient, message: MessageSaveSessionDto, request: AckRequest) {
        Log.info("Cliente ${client.sessionId} pediu para salvar a sessão $message")
        val sessionContext = sessions[client.sessionId]
        val content = message.content
        if (sessionContext == null || sessionContext.type != UserSessionType.USER || content == null) return

        // Solicitar para parar medições (caso ainda não tenha parado)
        sendStopRoom(sessionContext.room.toString())

        runCatching {
            var session = Session.findOrCreateInstance(sessionContext.id)

            if (session.physiotherapist != null && session.physiotherapist?.id != content.patientId) {
                return Log.warn("A sessão já foi salva para outro usuário")
            }

            if (session.isNewBean) {
                session.physiotherapist = User.findOrThrowById(sessionContext.userId!!)
                session.patient = User.findByPatientId(content.patientId!!)
                session = session.save()
            }

            val procedure = session.procedures.firstOrNull { it.type == content.procedure } ?: Procedure()
            procedure.type = content.procedure

            // Criar movimento sempre que solicitar para salvar
            val movement = Movement()
            movement.type = content.movementType
            movement.observation = content.movementObservation
            movement.movementType = MovementType.findByType(content.movementType)

            // Adicionar sensores ao movimento
            for ((first, second) in sessionContext.sensors.values) {
                val sensor = Sensor()
                val sensorInfo = SensorInfo.findByMacAddress(first.mac!!) ?: SensorInfo()

                if (sensorInfo.isNewBean) {
                    sensorInfo.macAddress = first.mac
                    sensorInfo.sensorName = first.name
                }

                sensor.ip = first.ip
                sensor.type = first.type
                sensor.sensorInfo = sensorInfo
                sensor.position = first.position
                sensor.observation = first.observation

                for (measurement in second.sortedBy { it.readOrder }) {
                    val measurement = Measurement.fromDto(measurement)
                    sensor.measurements.add(measurement)
                }

                movement.sensors.add(sensor)
            }

            // Adicionar movimentos ao procedimento
            procedure.movements.add(movement)

            // Adicionar procedimentos a sessão
            session.procedures.add(procedure)
            session.type = SessionType.REAL
            session.observation = content.observation
            session = session.save()
            sessionContext.id = session.id
        }

        request.sendAckData("SAVED_SESSION")
    }

    fun clientServerRemoveSensor(client: SocketIOClient, message: MessageRemoveSensorDto, request: AckRequest) {
        val session = sessions[client.sessionId]
        val content = message.content
        if (session == null || session.type != UserSessionType.USER || content == null) return

        val sensorContext = sessions[content.sensor]
        if (sensorContext != null) {
            val sensorClient = server.getClient(content.sensor)
            sensorClient.leaveRoom(content.sensor.toString())
            sensorClient.sendEvent(MessageType.SERVER_SENSOR_REMOVED_ROOM.description, MessageRemovedRoomDto())
            sensorContext.room = null

            Log.info("Cliente ${client.sessionId} saiu da sala $message")
            request.sendAckData("REMOVED_ROOM")
        }
    }

    fun clientServerAddSensor(client: SocketIOClient, message: MessageAddSensorDto, request: AckRequest) {
        val session = sessions[client.sessionId]
        val content = message.content
        if (session == null || session.type != UserSessionType.USER || content == null || session.room != null) return

        val sensorContext = sessions[content.sensor] ?: return Log.warn("Sensor não encontrado")
        val sensorClient = server.getClient(content.sensor) ?: return Log.warn("Cliente do sensor não encontrado")

        Log.info("Cliente ${client.sessionId} entrou na sala $message")

        sensorClient.joinRoom(session.room.toString())
        sensorClient.sendEvent(MessageType.SERVER_SENSOR_JOINED_ROOM.description, MessageJoinedRoomDto())
        sensorContext.room = session.room

        val sensor = findSensorByIp(sensorContext.sensorId.toString())
        if (sensor != null) session.sensors[content.sensor] = Pair(sensor, mutableSetOf())

        request.sendAckData("JOINED_ROOM")
    }

    fun clientServerCommandStart(client: SocketIOClient, message: String, request: AckRequest) {
        Log.info("Command to start measurements: $message")
        val session = sessions[client.sessionId]
        if (session == null || session.type != UserSessionType.USER) return

        // Limpar medições em cache ao iniciar novas medições
        runCatching {
            session.sensors
                .values
                .forEach {
                    it.second.clear()
                }
        }

        sendStartRoom(session.room.toString())
        request.sendAckData("STARTED_MEASUREMENTS")
    }

    fun clientServerCommandStop(client: SocketIOClient, message: String, request: AckRequest) {
        Log.info("Command to stop measurements: $message")
        val session = sessions[client.sessionId]
        if (session == null || session.type != UserSessionType.USER) return

        sendStopRoom(session.room.toString())
        request.sendAckData("STOPED_MEASUREMENTS")
    }

    private fun clientServerSensorList(client: SocketIOClient, message: String, request: AckRequest) {
        Log.info("Request sensor list: $message")
        val session = sessions[client.sessionId]
        if (session == null || session.type != UserSessionType.USER) return

        sendSensorList(client)
        request.sendAckData("REQUESTED_SENSOR_LIST")
    }

    // Sensor -> Server
    fun sensorServerMeasurement(client: SocketIOClient, message: MessageSensorMeasurementBlock, request: AckRequest) {
        Log.info("Register measurement listener: $message")
        val sessionId = client.sessionId
        val session = sessions[sessionId]
        val content = message.content
        if (content == null || (session == null || session.type != UserSessionType.SENSOR)) return

        val userClient = server.getClient(session.room) ?: return
        val userSession = sessions[session.room] ?: return

        val sensor = userSession.sensors[sessionId] ?: return
        sensor.second.addAll(content)

        val block = MessageClientMeasurementBlock()
        block.content = message.content
        userClient.sendEvent(MessageType.SERVER_CLIENT_MEASUREMENT.description, message)
    }

    private fun sensorServerRegisterSensor(client: SocketIOClient, message: MessageSessionSensorDto, request: AckRequest) {
        Log.info("Register sensor: $message")
        val session = sessions[client.sessionId]
        val content = message.content
        if (content == null || (session == null || session.type != UserSessionType.SENSOR)) return

        val sensorIp = content.ip
        if (sensorIp == null) return

        Log.info("Registered new sensor: $sensorIp")
        session.sensorId = sensorIp

        val existingSensor = findSensorByIp(sensorIp)
        if (existingSensor != null) {
            existingSensor.let {
                it.clientId = client.sessionId
                it.name = content.name
                it.mac = content.mac
                it.observation = content.observation
                it.position = content.position
                it.type = content.type
            }
            return request.sendAckData("UPDATED")
        }

        sensors.add(content)
        return request.sendAckData("REGISTERED")
    }


    private fun sendStartRoom(room: String) {
        val rom = server.getRoomOperations(room)
        rom.sendEvent(MessageType.SERVER_SENSOR_START.description, MessageStartCommandDto())
    }

    private fun sendStopRoom(room: String) {
        val rom = server.getRoomOperations(room)
        rom.sendEvent(MessageType.SERVER_SENSOR_STOP.description, MessageStopCommandDto())
    }

    private fun findSensorByIp(ip: String): SessionSensorDto? {
        return sensors.find { it.ip == ip }
    }

    private fun removeSensor(ip: String? = null): Boolean {
        return sensors.removeIf { it.ip == ip }
    }

    private fun sendSensorList(client: SocketIOClient) = runCatching {
        val message = MessageSensorListDto()
        message.content = sensors
        client.sendEvent(MessageType.SERVER_CLIENT_SENSOR_LIST.description, message)
    }

    private fun broadcastSensorList() = runCatching {
        val message = MessageSensorListDto()
        message.content = sensors
        server.broadcastOperations.sendEvent(MessageType.SERVER_CLIENT_SENSOR_LIST.description, message)
    }

}