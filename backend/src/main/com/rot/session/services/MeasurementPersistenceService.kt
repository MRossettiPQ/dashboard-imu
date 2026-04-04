package com.rot.session.services

import com.rot.measurement.dtos.MeasurementRead
import com.rot.measurement.models.Measurement
import com.rot.measurement.models.SensorInfo
import com.rot.session.models.SessionSensor
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@ApplicationScoped
class MeasurementPersistenceService(
    var entityManager: EntityManager
) {

    companion object {
        const val BUFFER_THRESHOLD = 500
        const val FLUSH_INTERVAL_SECONDS = 10L
    }

    private val measurementBuffers: ConcurrentHashMap<String, CopyOnWriteArrayList<MeasurementRead>> = ConcurrentHashMap()
    private val sessionSensorIdCache: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    private val flushing = AtomicBoolean(false)

    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "measurement-flush-scheduler").apply { isDaemon = true }
    }

    init {
        scheduler.scheduleAtFixedRate(
            { runCatching { flushAll() }.onFailure { Log.error("Erro no flush periódico", it) } },
            FLUSH_INTERVAL_SECONDS,
            FLUSH_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        )
    }

    // ─── Buffer ─────────────────────────────────────────────────────────
    fun buffer(sessionId: UUID, mac: String, measurements: List<MeasurementRead>) {
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
            measurementBuffers.keys.forEach { key -> flush(key) }
        } finally {
            flushing.set(false)
        }
    }

    // ─── Persistência ───────────────────────────────────────────────────

    @Transactional
    fun persistBatch(sessionId: UUID, mac: String, measurements: List<MeasurementRead>) {
        val sessionSensorId = resolveSessionSensorId(sessionId, mac)
        if (sessionSensorId == null) {
            Log.warn("SessionSensor não encontrado para sessão $sessionId e sensor $mac. Descartando ${measurements.size} medições.")
            return
        }

        val sessionSensor = entityManager.find(SessionSensor::class.java, sessionSensorId)
            ?: return Log.warn("SessionSensor ID $sessionSensorId não encontrado no banco")

        var count = 0
        for (dto in measurements) {
            val m = Measurement()
            m.sessionSensor = sessionSensor
            m.readOrder = dto.readOrder
            m.capturedAt = dto.capturedAt
            m.sensorName = mac
            m.accelMssX = dto.accelMssX
            m.accelMssY = dto.accelMssY
            m.accelMssZ = dto.accelMssZ
            m.accelLinX = dto.accelLinX
            m.accelLinY = dto.accelLinY
            m.accelLinZ = dto.accelLinZ
            m.gyroRadsX = dto.gyroRadsX
            m.gyroRadsY = dto.gyroRadsY
            m.gyroRadsZ = dto.gyroRadsZ
            m.magBiasX = dto.magBiasX
            m.magBiasY = dto.magBiasY
            m.magBiasZ = dto.magBiasZ
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

    private fun resolveSessionSensorId(sessionId: UUID, mac: String): Int? {
        val key = "$sessionId:$mac"
        return sessionSensorIdCache.computeIfAbsent(key) {
            val result = entityManager.createQuery(
                "SELECT ss.id FROM SessionSensor ss WHERE ss.session.id = :sessionId AND ss.sensorInfo.macAddress = :mac",
                Int::class.javaObjectType
            )
                .setParameter("sessionId", sessionId)
                .setParameter("mac", mac)
                .resultList
                .firstOrNull()
            result ?: -1
        }.takeIf { it != -1 }
    }

    fun invalidateCache(sessionId: UUID) {
        sessionSensorIdCache.keys.removeIf { it.startsWith("$sessionId:") }
    }

    // ─── Registro de sensores ───────────────────────────────────────────
    @Transactional
    fun registerSensor(mac: String, name: String?, ip: String?) {
        var sensorInfo = SensorInfo.Companion.findByMacAddress(mac)
        if (sensorInfo == null) {
            sensorInfo = SensorInfo()
            sensorInfo.macAddress = mac
        }
        sensorInfo.sensorName = name ?: "Sensor-$mac"
        sensorInfo.active = true
        sensorInfo.save()
        Log.info("SensorInfo registrado/atualizado: $mac (active=true)")
    }

    /**
     * Marca um sensor como inativo no banco.
     * Chamado quando o sensor desconecta do broker.
     */
    @Transactional
    fun deactivateSensor(mac: String) {
        val sensorInfo = SensorInfo.Companion.findByMacAddress(mac)
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
            Measurement.Companion.delete("sessionSensor.session.id = :sessionId AND sensorName = :mac", sessionId, mac)
        } catch (e: Exception) {
            Log.warn("Erro ao limpar dados de medição anteriores do sensor $mac", e)
        }
    }
}