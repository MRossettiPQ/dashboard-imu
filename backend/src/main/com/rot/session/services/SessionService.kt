package com.rot.session.services

import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.mqtt.services.MqttBrokerService
import com.rot.session.dtos.CreateSessionDto
import com.rot.session.dtos.UserSessionContext
import com.rot.session.enums.SessionStatus
import com.rot.session.models.Session
import com.rot.session.models.SessionSensor
import com.rot.measurement.models.SensorInfo
import com.rot.session.services.MeasurementPersistenceService
import com.rot.user.models.User
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.*

@ApplicationScoped
class SessionService(
    private val mqttBrokerService: MqttBrokerService,
    private val measurementPersistenceService: MeasurementPersistenceService
) {

    @Transactional
    fun create(body: CreateSessionDto): Session {
        val ctx = ApplicationContext.context!!

        // Verifica se já existe sessão ativa para o fisioterapeuta
        var session = Session.findByPhysiotherapistId(ctx.user.id!!)

        // Se existe mas é para outro paciente ou expirou (>60min), cria nova
        if (session != null && session.patient?.id != body.patientId || session?.sessionDate?.isBefore(OffsetDateTime.now().minusMinutes(60)) == true) {
            // Finaliza sessão anterior
            finalize(session.id!!)
            session = null
        }

        if (session == null) {
            session = Session()
            session.status = SessionStatus.CREATED
            session.patient = User.findOrThrowById(body.patientId)
            session.physiotherapist = User.findOrThrowById(ctx.user.id!!)
            session.type = body.type
        }

        session.observation = body.observation
        session = session.save()

        // Registra no contexto MQTT em memória
        val userCtx = UserSessionContext().apply {
            id = session.id
            userId = ctx.user.id
            patientId = body.patientId
        }
        mqttBrokerService.activeSessions[session.id!!] = userCtx

        // Publica status
        mqttBrokerService.publishSessionStatus(session.id!!, SessionStatus.CREATED.name)

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
        val sensorInfo = SensorInfo.findByMacAddress(macAddress) ?: throw ApplicationException("Sensor não encontrado", Response.Status.NOT_FOUND)

        // REGRA: Verificar disponibilidade antes de comandar
        val sensorContext = mqttBrokerService.connectedSensors[macAddress]
            ?: throw ApplicationException("Sensor $macAddress não está conectado ao broker", Response.Status.BAD_REQUEST)

        if (!sensorContext.available) {
            throw ApplicationException("Sensor $macAddress está em uso por outra sessão", Response.Status.CONFLICT)
        }

        // NOVO: Limpar medições anteriores do buffer/banco para esta sessão+sensor
        measurementPersistenceService.clearSensorData(sessionId, macAddress)

        val sessionSensor = SessionSensor().apply {
            this.session = session
            this.sensorInfo = sensorInfo
            this.ip = sensorContext.ip ?: "unknown"
        }
        sessionSensor.save()

        // Informar o sensor. O ESP32 agora fará o subscribe no canal da sessão ao receber o ASSIGN
        mqttBrokerService.assignSensorToSession(macAddress, sessionId)
        measurementPersistenceService.invalidateCache(sessionId)

        return sessionSensor
    }

    @Transactional
    fun removeSensor(sessionId: UUID, macAddress: String) {
        // REGRA: Verificar se ele esta na sessão atual
        val sessionCtx = mqttBrokerService.activeSessions[sessionId]
        if (sessionCtx?.assignedSensors?.contains(macAddress) != true) {
            throw ApplicationException("Sensor não pertence a esta sessão", Response.Status.BAD_REQUEST)
        }

        mqttBrokerService.releaseSensorFromSession(macAddress) // Envia RELEASE, ESP32 faz unsubscribe
        measurementPersistenceService.invalidateCache(sessionId)
    }

    fun calibrateSensor(macAddress: String) {
        val sensor = mqttBrokerService.connectedSensors[macAddress]
            ?: throw ApplicationException("Sensor $macAddress não está conectado", Response.Status.NOT_FOUND)

        // REGRA: Ao enviar comando de calibrar, deve fazer broadcast de stop para o canal da sessão.
        if (sensor.sessionId != null) {
            Log.info("Sensor $macAddress em calibração. Parando sessão atual ${sensor.sessionId} via broadcast.")
            mqttBrokerService.sendCommandToSession(sensor.sessionId!!, "STOP")
            // Opcional: Atualizar o status da Session no banco para PAUSED ou STOPPED
        }

        // Manda o comando direto no /sensor/<mac>/command
        mqttBrokerService.sendCommandToSensor(macAddress, "CALIBRATE")
        sensor.available = false
        mqttBrokerService.publishAvailableSensors()
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
        mqttBrokerService.sendCommandToSession(sessionId, "START")
        mqttBrokerService.publishSessionStatus(sessionId, SessionStatus.RUNNING.name)

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
        mqttBrokerService.sendCommandToSession(sessionId, "STOP")
        mqttBrokerService.publishSessionStatus(sessionId, SessionStatus.COMPLETED.name)

        // Flush das medições pendentes no buffer
        measurementPersistenceService.clearSession(sessionId)

        Log.info("Sessão $sessionId parada")
    }

    /**
     * Finaliza completamente uma sessão: para sensores, flush dados, libera sensores.
     */
    @Transactional
    fun finalize(sessionId: UUID) {
        val session = Session.findOrThrowById(sessionId)

        // Para todos os sensores
        mqttBrokerService.sendCommandToSession(sessionId, "STOP")

        // Flush medições pendentes
        measurementPersistenceService.clearSession(sessionId)

        // Libera todos os sensores da sessão
        val sessionCtx = mqttBrokerService.activeSessions[sessionId]
        sessionCtx?.assignedSensors?.toList()?.forEach { mac ->
            mqttBrokerService.releaseSensorFromSession(mac)
        }

        // Atualiza status
        session.status = SessionStatus.COMPLETED
        session.save()

        // Remove do contexto em memória
        mqttBrokerService.activeSessions.remove(sessionId)
        mqttBrokerService.publishSessionStatus(sessionId, SessionStatus.COMPLETED.name)

        Log.info("Sessão $sessionId finalizada")
    }
}