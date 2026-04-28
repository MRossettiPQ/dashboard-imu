package com.rot.session.services

import com.rot.core.config.ApplicationConfig
import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.utils.JsonUtils
import com.rot.measurement.dtos.MeasurementRead
import com.rot.measurement.models.Measurement
import com.rot.measurement.models.SensorInfo
import com.rot.mqtt.services.JwtMqttAuthenticator
import com.rot.mqtt.services.MqttInterceptor
import com.rot.session.dtos.ClientSessionContext
import com.rot.session.dtos.SessionCreateOrUpdate
import com.rot.session.dtos.SessionFinalizeUpdate
import com.rot.session.dtos.UserSessionContext
import com.rot.session.enums.BodySegmentationEnum
import com.rot.session.enums.SessionContextType
import com.rot.session.enums.SessionStatus
import com.rot.session.models.NodeSensor
import com.rot.session.models.Session
import com.rot.session.models.SessionNode
import com.rot.session.models.SessionSensor
import com.rot.user.models.Patient
import com.rot.user.models.User
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttQoS
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@ApplicationScoped
class SessionService(
    var entityManager: EntityManager,
    private val applicationConfig: ApplicationConfig
) {

    companion object {
        const val BUFFER_THRESHOLD = 500
        const val FLUSH_INTERVAL_SECONDS = 10L
    }

    private lateinit var mqttBroker: Server

    val contexts: ConcurrentHashMap<String, ClientSessionContext> = ConcurrentHashMap()
    private val measurementBuffers: ConcurrentHashMap<String, CopyOnWriteArrayList<MeasurementRead>> = ConcurrentHashMap()

    fun createThread(task: Runnable): Thread {
        val thread = Thread(task, "measurement-buffer-scheduler")
        thread.isDaemon = true
        return thread
    }

    private val flushing = AtomicBoolean(false)
    private val scheduler = Executors.newSingleThreadScheduledExecutor(::createThread)

    fun getContextByKey(key: String): ClientSessionContext? = contexts[key]

    fun getContextBySessionId(sessionId: UUID, type: SessionContextType): ClientSessionContext? {
        return contexts.values.firstOrNull {
            it.sessionId == sessionId && it.type == type
        }
    }

    init {
        scheduler.scheduleAtFixedRate(
            { runCatching { flushAll() }.onFailure { Log.error("Erro no flush periódico", it) } },
            FLUSH_INTERVAL_SECONDS,
            FLUSH_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        )
    }

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
            flushAll()
            mqttBroker.stopServer()
            Log.info("MQTT Broker parado")
        }
    }

    @Transactional
    fun create(body: SessionCreateOrUpdate): Session {
        val ctx = ApplicationContext.context!!

        // Verifica se já existe sessão ativa para o fisioterapeuta
        var session = Session.findByPhysiotherapistId(ctx.user.id!!)
        val patient = Patient.findOrThrowById(body.patientId)

        // Se existe, mas é para outro paciente ou expirou (>60min), cria
        if (session != null && session.patient?.id != patient.user!!.id || session?.sessionDate?.isBefore(OffsetDateTime.now().minusMinutes(60)) == true) {
            session.delete()
            session = null
        }

        if (session == null) {
            session = Session()
            session.status = SessionStatus.CREATED
            session.patient = patient.user
            session.physiotherapist = User.findOrThrowById(ctx.user.id!!)
            session.type = body.type
        }

        session.observation = body.observation
        session = session.save()

        contexts[ctx.user.id!!.toString()] = ClientSessionContext().apply {
            this.sessionId = session.id!!
            this.type = SessionContextType.USER
            this.user = UserSessionContext().apply {
                this.userId = ctx.user.id!!
                this.patientId = body.patientId
            }
        }

        Log.info("Sessão criada: ${session.id} (paciente: ${body.patientId})")
        return session
    }

    /**
     * Adiciona um sensor a uma sessão.
     * Cria o SessionSensor no banco e atribui o sensor no broker MQTT.
     */
    @Transactional
    fun addSensor(sessionId: UUID, macAddress: String): SessionSensor {
        val session = Session.findOrThrowById(sessionId)
        val sensorInfo = SensorInfo.findByMacAddress(macAddress)
            ?: throw ApplicationException("Sensor não encontrado", Response.Status.NOT_FOUND)

        val existingSessionSensor = SessionSensor.createQuery()
            .where(SessionSensor.q.session().id.eq(sessionId))
            .where(SessionSensor.q.sensorInfo().macAddress.eq(macAddress))
            .fetchFirst()

        if (existingSessionSensor != null) {
            return existingSessionSensor
        }

        // REGRA: Verificar disponibilidade antes de comandar
        val sensorContext = contexts[macAddress]
            ?: throw ApplicationException("Sensor $macAddress não está conectado ao broker", Response.Status.BAD_REQUEST)

        if (!sensorContext.isAvailable() && sensorContext.sessionId != sessionId) {
            throw ApplicationException("Sensor $macAddress está em uso por outra sessão", Response.Status.CONFLICT)
        }

        // NOVO: Limpar medições anteriores do buffer/banco para esta sessão+sensor
        clearSensorData(sessionId, macAddress)

        val sessionSensor = SessionSensor().apply {
            this.session = session
            this.sensorInfo = sensorInfo
            this.ip = sensorContext.sensor!!.ip ?: "unknown"
        }
        sessionSensor.save()

        // Informar o sensor. O ESP32 agora fará o subscribe no canal da sessão ao receber o ASSIGN
        assignSensorToSession(macAddress, sessionId)
        invalidateCache(sessionId)

        return sessionSensor
    }

    @Transactional
    fun removeSensor(sessionId: UUID, macAddress: String) {
        val session = Session.findOrThrowById(sessionId)

        val sessionCtx = getContextBySessionId(sessionId, SessionContextType.USER) ?: return
        if (sessionCtx.isSensor()) return

        if (!sessionCtx.user!!.assignedSensors.contains(macAddress)) {
            throw ApplicationException("Sensor não pertence a esta sessão", Response.Status.BAD_REQUEST)
        }

        if (session.status != SessionStatus.COMPLETED) {
            // 1. Limpa buffer em memória + medições no banco
            clearSensorData(sessionId, macAddress)

            // 2. Remove o SessionSensor do banco
            SessionSensor.createQuery()
                .where(SessionSensor.q.session().id.eq(sessionId))
                .where(SessionSensor.q.sensorInfo().macAddress.eq(macAddress))
                .fetchFirst()
                ?.let { sessionSensor ->
                    // Cascade deleta os Measurements e NodeSensors associados
                    entityManager.remove(entityManager.merge(sessionSensor))
                }

            Log.info("SessionSensor e medições removidos para sensor $macAddress na sessão $sessionId")
        }

        // Libera o sensor no broker (envia RELEASE pro ESP32)
        releaseSensorFromSession(macAddress)
        invalidateCache(sessionId)
    }

    fun calibrateSensor(macAddress: String) {
        val ctx = getContextByKey(macAddress)
            ?: throw ApplicationException("Sensor $macAddress não está conectado", Response.Status.NOT_FOUND)

        if (ctx.sessionId != null) {
            Log.info("Sensor $macAddress em calibração. Parando sessão ${ctx.sessionId}.")
            sendCommandToSession(ctx.sessionId!!, "STOP")
        }

        publish("sensor/$macAddress/calibrate", mapOf("command" to "CALIBRATE")) // ✅ já correto
        ctx.setAvailable(false)
        publishAvailableSensors()
    }

    /**
     * Inicia a coleta de medições: envia comando START a todos os sensores da sessão.
     */
    @Transactional
    fun start(sessionId: UUID) {
        val session = Session.findOrThrowById(sessionId)

        if (session.status == SessionStatus.RUNNING) {
            throw ApplicationException("Sessão já está em execução", Response.Status.CONFLICT)
        }

        session.status = SessionStatus.RUNNING
        session.save()

        // Envia START para todos os sensores via MQTT
        sendCommandToSession(sessionId, "START")
        publishSessionStatus(sessionId, SessionStatus.RUNNING.name)

        Log.info("Sessão $sessionId iniciada")
    }

    /**
     * Para a coleta de medições: envia comando STOP a todos os sensores da sessão.
     */
    @Transactional
    fun stop(sessionId: UUID) {
        val session = Session.findOrThrowById(sessionId)

        session.status = SessionStatus.COMPLETED
        session.save()

        // Envia STOP para todos os sensores via MQTT
        sendCommandToSession(sessionId, "STOP")
        publishSessionStatus(sessionId, SessionStatus.COMPLETED.name)

        // Flush das medições pendentes no buffer
        clearSession(sessionId)

        Log.info("Sessão $sessionId parada")
    }

    /**
     * Finaliza completamente uma sessão: para sensores, flush dados, libera sensores.
     */
    @Transactional
    fun finalize(sessionId: UUID, body: SessionFinalizeUpdate) {
        val session = Session.findOrThrowById(sessionId)

        sendCommandToSession(sessionId, "STOP")
        clearSession(sessionId)

        val sessionCtx = getContextBySessionId(sessionId, SessionContextType.USER) ?: return

        sessionCtx.user!!.assignedSensors
            .toList()
            .forEach(::releaseSensorFromSession)

        // LÓGICA DE CRIAÇÃO DE NÓS (SessionNode e NodeSensor)
        for (nodeDto in body.sessionNodes) {
            // Cria o SessionNode (A articulação: Joelho, Cotovelo, etc)
            var sessionNode = SessionNode().apply {
                this.session = session
                // KNEE_LEFT, ELBOW_RIGHT, etc.
                this.region = nodeDto.region
            }
            sessionNode = sessionNode.save()

            // Associa os sensores a este nó (NodeSensor)
            // Note que o DTO foi ajustado para receber os MACs dos sensores que compõem o nó
            nodeDto.sensorMacs.forEach { macPair ->
                val mac = macPair.key
                // Qual segmento este sensor representa no nó (ex: LEFT_THIGH)
                val segmentation = macPair.value

                // Busca o SessionSensor existente para esta sessão e MAC
                val sessionSensor = SessionSensor.createQuery()
                    .where(SessionSensor.q.session().id.eq(sessionId))
                    .where(SessionSensor.q.sensorInfo().macAddress.eq(mac))
                    .fetchFirst()

                if (sessionSensor != null) {
                    val nodeSensor = NodeSensor().apply {
                        this.sessionNode = sessionNode
                        this.sessionSensor = sessionSensor
                        this.segmentation = BodySegmentationEnum.valueOf(segmentation)
                    }
                    nodeSensor.save()
                } else {
                    Log.warn("Sensor $mac não encontrado na sessão $sessionId ao tentar criar o nó ${sessionNode.region}.")
                }
            }
        }

        session.status = SessionStatus.COMPLETED
        session.save()

        // Remove do contexts em vez de activeSessions
        contexts.values.removeIf { it.sessionId == sessionId && it.isUser() }

        publishSessionStatus(sessionId, SessionStatus.COMPLETED.name)
        Log.info("Sessão $sessionId finalizada e ${body.sessionNodes.size} nós criados.")
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
        val available = contexts.values
            .filter { it.isSensor() && it.isAvailable() }
        publish("sensors/available", mapOf("sensors" to available))
    }

    // ─── Gerenciamento de sensores ──────────────────────────────────────
    fun assignSensorToSession(macAddress: String, sessionId: UUID): Boolean {
        val sensor = getContextByKey(macAddress)
        if (sensor == null) {
            Log.warn("Sensor $macAddress não encontrado")
            return false
        }

        if (!sensor.isAvailable()) {
            Log.warn("Sensor $macAddress já está em uso na sessão ${sensor.sessionId}")
            return false
        }

        val session = getContextBySessionId(sessionId, SessionContextType.USER)
            ?: throw ApplicationException("Sessão inexistente", Response.Status.NOT_FOUND)

        sensor.setAvailable(false)
        sensor.sessionId = sessionId

        session.user!!.assignedSensors.add(macAddress)

        sendCommandToSensor(macAddress, "ASSIGN", sessionId)
        publishAvailableSensors()
        Log.info("Sensor $macAddress atribuído à sessão $sessionId")
        return true
    }

    fun releaseSensorFromSession(macAddress: String): Boolean {
        val sensor = contexts[macAddress] ?: return false

        val sessionId = sensor.sessionId
        sensor.setAvailable(true)
        sensor.sessionId = null

        if (sessionId != null) {
            val session = getContextBySessionId(sessionId, SessionContextType.USER)
                ?: throw ApplicationException("Sessão inexistente", Response.Status.NOT_FOUND)

            session.user!!.assignedSensors.remove(macAddress)
        }

        sendCommandToSensor(macAddress, "RELEASE")
        publishAvailableSensors()
        Log.info("Sensor $macAddress liberado da sessão $sessionId")
        return true
    }


    fun handleSensorDisconnect(mac: String) {
        /**
         * Limpeza completa quando um sensor desconecta (chamado pelo interceptor).
         * Faz flush de medições pendentes, libera da sessão e remove dos mapas.
         */

        val ctx = contexts.remove(mac) ?: return
        val sessionId = ctx.sessionId

        if (sessionId != null) {
            flush("$sessionId:$mac")
            Log.warn("Sensor $mac caiu. Parando sessão $sessionId via broadcast.")
            sendCommandToSession(sessionId, "STOP")
            publishSessionStatus(sessionId, "STOPPED_BY_ERROR")

            // Remove o sensor da lista de assignedSensors da sessão
            getContextBySessionId(sessionId, SessionContextType.USER)
                ?.user?.assignedSensors?.remove(mac)
        }

        deactivateSensor(mac)
        publishAvailableSensors()
    }

    fun handleUserDisconnect(userId: String) {
        // Limpeza quando um frontend/usuário desconecta.
        contexts.remove(userId)
        Log.info("Usuário $userId desconectado — contexto removido")
    }

    fun buffer(sessionId: UUID, mac: String, measurements: List<MeasurementRead>) {
        // Rejeita medições de sensores não atribuídos à sessão
        val sensorCtx = contexts[mac]
        if (sensorCtx?.sessionId != sessionId) {
            Log.warn("Medições rejeitadas: sensor $mac não está na sessão $sessionId")
            return
        }

        val key = "$sessionId:$mac"
        val buffer = measurementBuffers.computeIfAbsent(key) { CopyOnWriteArrayList() }
        buffer.addAll(measurements)

        Log.debug("Buffer [$key]: ${buffer.size} medições acumuladas")

        if (buffer.size >= BUFFER_THRESHOLD) {
            flush(key)
        }
    }

    fun flush(key: String) {
        val buffer = measurementBuffers[key] ?: return
        if (buffer.isEmpty()) return

        val toFlush = ArrayList<MeasurementRead>(buffer.size)
        val iterator = buffer.iterator()
        while (iterator.hasNext()) {
            toFlush.add(iterator.next())
        }
        buffer.clear()

        if (toFlush.isEmpty()) return

        val parts = key.split(":")
        if (parts.size != 2) return

        val sessionId = UUID.fromString(parts[0])
        val mac = parts[1]

        try {
            persistBatch(sessionId, mac, toFlush)
            Log.info("Flush [$key]: ${toFlush.size} medições persistidas")
        } catch (e: Exception) {
            Log.error("Falha no flush [$key]: ${e.message}. Devolvendo ${toFlush.size} ao buffer.", e)
            val currentBuffer = measurementBuffers.computeIfAbsent(key) { CopyOnWriteArrayList() }
            currentBuffer.addAll(0, toFlush)
        }
    }

    fun flushAll() {
        if (!flushing.compareAndSet(false, true)) return
        try {
            measurementBuffers.keys.forEach(::flush)
        } finally {
            flushing.set(false)
        }
    }

    // ─── Persistência ───────────────────────────────────────────────────

    @Transactional
    fun persistBatch(sessionId: UUID, mac: String, measurements: List<MeasurementRead>) {
        val sessionSensor = SessionSensor.createQuery()
            .where(SessionSensor.q.session().id.eq(sessionId))
            .where(SessionSensor.q.sensorInfo().macAddress.eq(mac))
            .fetchFirst() ?: return

        var count = 0
        for (dto in measurements) {
            val m = Measurement()
            m.sessionSensor = sessionSensor
            m.readOrder = dto.readOrder
            m.capturedAt = dto.capturedAt
            m.sensorName = mac
            m.accelX = dto.accelX
            m.accelY = dto.accelY
            m.accelZ = dto.accelZ
            m.accelMssX = dto.accelMssX
            m.accelMssY = dto.accelMssY
            m.accelMssZ = dto.accelMssZ
            m.accelLinX = dto.accelLinX
            m.accelLinY = dto.accelLinY
            m.accelLinZ = dto.accelLinZ
            m.gyroRadsX = dto.gyroRadsX
            m.gyroRadsY = dto.gyroRadsY
            m.gyroRadsZ = dto.gyroRadsZ
            m.magX = dto.magX
            m.magY = dto.magY
            m.magZ = dto.magZ
            m.roll = dto.roll
            m.pitch = dto.pitch
            m.yaw = dto.yaw
            m.eulerX = dto.eulerX
            m.eulerY = dto.eulerY
            m.eulerZ = dto.eulerZ
            m.quaternionW = dto.quaternionW
            m.quaternionX = dto.quaternionX
            m.quaternionY = dto.quaternionY
            m.quaternionZ = dto.quaternionZ

            entityManager.persist(m)
            count++

            if (count % 100 == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }

        entityManager.flush()
        entityManager.clear()
    }

    fun invalidateCache(sessionId: UUID) {

    }

    // ─── Registro de sensores ───────────────────────────────────────────
    @Transactional
    fun registerSensor(mac: String, name: String?): SensorInfo {
        var sensorInfo = SensorInfo.findByMacAddress(mac)
        if (sensorInfo == null) {
            sensorInfo = SensorInfo()
            sensorInfo.macAddress = mac
        }
        sensorInfo.sensorName = name ?: "Sensor-$mac"
        sensorInfo.active = true
        sensorInfo = sensorInfo.save()
        Log.info("SensorInfo registrado/atualizado: $mac (active=true)")
        return sensorInfo
    }

    /**
     * Marca um sensor como inativo no banco.
     * Chamado quando o sensor desconecta do broker.
     */
    @Transactional
    fun deactivateSensor(mac: String) {
        val sensorInfo = SensorInfo.findByMacAddress(mac)
        if (sensorInfo != null) {
            sensorInfo.active = false
            sensorInfo.save()
            Log.info("SensorInfo desativado: $mac (active=false)")
        }
    }

    fun clearSession(sessionId: UUID) {
        measurementBuffers.keys
            .filter { it.startsWith("$sessionId:") }
            .forEach { flush(it) }

        measurementBuffers.keys.removeIf { it.startsWith("$sessionId:") }
        invalidateCache(sessionId)
    }

    fun clearSensorData(sessionId: UUID, mac: String) {
        val key = "$sessionId:$mac"

        // 1. Limpa o buffer de memória
        measurementBuffers.remove(key)

        // 2. Limpa dados antigos que possam ter ficado no banco (se o usuário readicionou o sensor)
        try {
            Measurement.delete("sessionSensor.session.id = :sessionId AND sensorName = :mac", sessionId, mac)
        } catch (e: Exception) {
            Log.warn("Erro ao limpar dados de medição anteriores do sensor $mac", e)
        }
    }

}