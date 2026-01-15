package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.AnatomicalPlaneEnum
import com.rot.session.enums.BodySegmentEnum
import com.rot.session.enums.MovementEnum
import com.rot.session.enums.ProcedureEnum
import com.rot.session.models.Measurement
import com.rot.session.models.Movement
import com.rot.session.models.Procedure
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.*
import kotlin.math.withSign


open class MeasurementDto {
    var id: UUID? = null
    var capturedAt: OffsetDateTime? = null
    var sensorName: String? = null
    var readOrder: Int? = null
    var accelMssX: BigDecimal? = null
    var accelMssY: BigDecimal? = null
    var accelMssZ: BigDecimal? = null
    var accelLinX: BigDecimal? = null
    var accelLinY: BigDecimal? = null
    var accelLinZ: BigDecimal? = null
    var gyroRadsX: BigDecimal? = null
    var gyroRadsY: BigDecimal? = null
    var gyroRadsZ: BigDecimal? = null
    var roll: BigDecimal? = null
    var pitch: BigDecimal? = null
    var yaw: BigDecimal? = null
    var eulerX: BigDecimal? = null
    var eulerY: BigDecimal? = null
    var eulerZ: BigDecimal? = null
    var quaternionX: BigDecimal? = null
    var quaternionY: BigDecimal? = null
    var quaternionZ: BigDecimal? = null
    var quaternionW: BigDecimal? = null

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MeasurementDto) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }

    companion object {
        fun from(measurement: Measurement) : MeasurementDto {
            return JsonUtils.MAPPER.convertValue(measurement, MeasurementDto::class.java)
        }
        fun from(paginationDto: PaginationDto<Measurement>): PaginationDto<MeasurementDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados do paciente")
class MeasurementResponse : ContentDto<MeasurementDto>()

@Schema(description = "Resposta com dados do paciente")
class MeasurementPaginationResponse : PaginationDto<MeasurementDto>()

// ============================================
// DATA CLASSES
// ============================================

data class Quaternion(
    val w: Double,
    val x: Double,
    val y: Double,
    val z: Double
) {
    fun conjugate() = Quaternion(w, -x, -y, -z)

    fun normalize(): Quaternion {
        val mag = sqrt(w * w + x * x + y * y + z * z)
        return if (mag > 0) Quaternion(w / mag, x / mag, y / mag, z / mag) else this
    }

    operator fun times(q: Quaternion): Quaternion {
        return Quaternion(
            w = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z,
            x = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y,
            y = this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x,
            z = this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w
        )
    }

    fun toEulerDegrees(): EulerAngles {
        val sinrCosp = 2.0 * (w * x + y * z)
        val cosrCosp = 1.0 - 2.0 * (x * x + y * y)
        val roll = atan2(sinrCosp, cosrCosp)

        val sinp = 2.0 * (w * y - z * x)
        val pitch = if (abs(sinp) >= 1.0) (PI / 2).withSign(sinp) else asin(sinp)

        val sinyCosp = 2.0 * (w * z + x * y)
        val cosyCosp = 1.0 - 2.0 * (y * y + z * z)
        val yaw = atan2(sinyCosp, cosyCosp)

        return EulerAngles(
            roll = Math.toDegrees(roll),
            pitch = Math.toDegrees(pitch),
            yaw = Math.toDegrees(yaw)
        )
    }

    companion object {
        val IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)

        fun fromMeasurement(m: Measurement): Quaternion {
            return Quaternion(
                w = m.quaternionW?.toDouble() ?: 1.0,
                x = m.quaternionX?.toDouble() ?: 0.0,
                y = m.quaternionY?.toDouble() ?: 0.0,
                z = m.quaternionZ?.toDouble() ?: 0.0
            ).normalize()
        }

        fun fromEulerDegrees(roll: Double, pitch: Double, yaw: Double): Quaternion {
            val cy = cos(Math.toRadians(yaw) * 0.5)
            val sy = sin(Math.toRadians(yaw) * 0.5)
            val cp = cos(Math.toRadians(pitch) * 0.5)
            val sp = sin(Math.toRadians(pitch) * 0.5)
            val cr = cos(Math.toRadians(roll) * 0.5)
            val sr = sin(Math.toRadians(roll) * 0.5)

            return Quaternion(
                w = cr * cp * cy + sr * sp * sy,
                x = sr * cp * cy - cr * sp * sy,
                y = cr * sp * cy + sr * cp * sy,
                z = cr * cp * sy - sr * sp * cy
            )
        }
    }
}

data class EulerAngles(
    val roll: Double,   // Plano Frontal
    val pitch: Double,  // Plano Sagital
    val yaw: Double     // Plano Transversal
) {
    fun getByPlane(plane: AnatomicalPlaneEnum): Double {
        return when (plane) {
            AnatomicalPlaneEnum.SAGITTAL -> pitch
            AnatomicalPlaneEnum.FRONTAL -> roll
            AnatomicalPlaneEnum.TRANSVERSE -> yaw
        }
    }
}

/**
 * Resultado do cálculo de ângulo articular
 */
data class JointAngleResult(
    val procedure: ProcedureEnum,
    val movement: MovementEnum,
    val timestamp: OffsetDateTime?,
    val angle: Double,                    // Ângulo no plano do movimento
    val allAngles: EulerAngles,           // Todos os ângulos (para debug/análise)
    val proximalSegment: BodySegmentEnum,
    val distalSegment: BodySegmentEnum
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "procedure" to procedure.description,
        "movement" to movement.description,
        "plane" to movement.plane.description,
        "timestamp" to timestamp,
        "angle" to angle.roundTo(2),
        "sagittal" to allAngles.pitch.roundTo(2),
        "frontal" to allAngles.roll.roundTo(2),
        "transverse" to allAngles.yaw.roundTo(2),
        "proximalSegment" to proximalSegment.description,
        "distalSegment" to distalSegment.description
    )
}

/**
 * Resultado de uma série temporal de ângulos
 */
data class JointAngleTimeSeries(
    val procedure: ProcedureEnum,
    val movement: MovementEnum,
    val samples: List<JointAngleResult>,
    val statistics: AngleStatistics?
)

data class AngleStatistics(
    val min: Double,
    val max: Double,
    val mean: Double,
    val range: Double,      // ROM - Range of Motion
    val stdDev: Double
) {
    companion object {
        fun calculate(angles: List<Double>): AngleStatistics? {
            if (angles.isEmpty()) return null
            val min = angles.minOrNull() ?: 0.0
            val max = angles.maxOrNull() ?: 0.0
            val mean = angles.average()
            val variance = angles.map { (it - mean).pow(2) }.average()
            return AngleStatistics(
                min = min,
                max = max,
                mean = mean,
                range = max - min,
                stdDev = sqrt(variance)
            )
        }
    }
}

// ============================================
// SERVIÇO PRINCIPAL
// ============================================

class JointAngleService {

    /**
     * Calcula o ângulo articular para um Movement específico
     *
     * @param movement O Movement contendo os sensores e medições
     * @return Lista de resultados de ângulos para cada par de medições sincronizadas
     */
    fun calculateJointAngles(movement: Movement): JointAngleTimeSeries {
        val procedure = movement.procedure?.type
            ?: throw IllegalArgumentException("Movement must have a procedure")
        val movementType = movement.type
            ?: throw IllegalArgumentException("Movement must have a type")

        // Encontra sensores proximal e distal
        val sensors = movement.sensors.toList()
        val proximalSensor = sensors.find { it.isProximal }
            ?: throw IllegalArgumentException("No proximal sensor found")
        val distalSensor = sensors.find { !it.isProximal }
            ?: throw IllegalArgumentException("No distal sensor found")

        // Valida segmentos
        val proximalSegment = proximalSensor.bodySegment
            ?: throw IllegalArgumentException("Proximal sensor has no body segment")
        val distalSegment = distalSensor.bodySegment
            ?: throw IllegalArgumentException("Distal sensor has no body segment")

        // Sincroniza medições
        val alignedMeasurements = alignMeasurements(
            proximalSensor.measurements.toList(),
            distalSensor.measurements.toList()
        )

        // Calcula ângulos
        val samples = alignedMeasurements.map { (proximalMeasurement, distalMeasurement) ->
            calculateAngle(
                procedure = procedure,
                movementType = movementType,
                proximalMeasurement = proximalMeasurement,
                distalMeasurement = distalMeasurement,
                proximalSegment = proximalSegment,
                distalSegment = distalSegment
            )
        }

        // Calcula estatísticas
        val statistics = AngleStatistics.calculate(samples.map { it.angle })

        return JointAngleTimeSeries(
            procedure = procedure,
            movement = movementType,
            samples = samples,
            statistics = statistics
        )
    }

    /**
     * Calcula ângulo para um único par de medições
     */
    fun calculateAngle(
        procedure: ProcedureEnum,
        movementType: MovementEnum,
        proximalMeasurement: Measurement,
        distalMeasurement: Measurement,
        proximalSegment: BodySegmentEnum,
        distalSegment: BodySegmentEnum
    ): JointAngleResult {
        // Extrai quaternions
        val qProximal = extractQuaternion(proximalMeasurement)
        val qDistal = extractQuaternion(distalMeasurement)

        // Calcula quaternion relativo: q_rel = q_distal * q_proximal^(-1)
        val qRelative = qDistal * qProximal.conjugate()

        // Converte para Euler
        val eulerAngles = qRelative.toEulerDegrees()

        // Extrai o ângulo do plano correto baseado no tipo de movimento
        val angleInPlane = eulerAngles.getByPlane(movementType.plane)

        // Ajusta sinal baseado na direção do movimento
        val adjustedAngle = if (movementType.positiveDirection) {
            angleInPlane
        } else {
            -angleInPlane
        }

        return JointAngleResult(
            procedure = procedure,
            movement = movementType,
            timestamp = proximalMeasurement.capturedAt ?: distalMeasurement.capturedAt,
            angle = adjustedAngle.normalizeAngle(),
            allAngles = eulerAngles,
            proximalSegment = proximalSegment,
            distalSegment = distalSegment
        )
    }

    /**
     * Calcula ângulos para todos os movements de um Procedure
     */
    fun calculateProcedureAngles(procedure: Procedure): Map<MovementEnum, JointAngleTimeSeries> {
        return procedure.movements
            .filter { it.sensors.size >= 2 }
            .associate { movement ->
                val movementType = movement.type ?: MovementEnum.SIMPLE
                movementType to calculateJointAngles(movement)
            }
    }

    /**
     * Compara ROM (Range of Motion) entre lados do corpo
     */
    fun compareROM(
        leftMovement: Movement,
        rightMovement: Movement
    ): ROMComparison {
        val leftSeries = calculateJointAngles(leftMovement)
        val rightSeries = calculateJointAngles(rightMovement)

        return ROMComparison(
            movement = leftMovement.type ?: MovementEnum.SIMPLE,
            leftROM = leftSeries.statistics?.range ?: 0.0,
            rightROM = rightSeries.statistics?.range ?: 0.0,
            difference = abs((leftSeries.statistics?.range ?: 0.0) -
                    (rightSeries.statistics?.range ?: 0.0)),
            leftStats = leftSeries.statistics,
            rightStats = rightSeries.statistics
        )
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private fun extractQuaternion(measurement: Measurement): Quaternion {
        // Tenta quaternion direto
        if (measurement.quaternionW != null) {
            return Quaternion.fromMeasurement(measurement)
        }

        // Fallback para Euler
        val roll = measurement.roll?.toDouble()
            ?: measurement.eulerX?.toDouble()
            ?: 0.0
        val pitch = measurement.pitch?.toDouble()
            ?: measurement.eulerY?.toDouble()
            ?: 0.0
        val yaw = measurement.yaw?.toDouble()
            ?: measurement.eulerZ?.toDouble()
            ?: 0.0

        return Quaternion.fromEulerDegrees(roll, pitch, yaw)
    }

    private fun alignMeasurements(
        proximalMeasurements: List<Measurement>,
        distalMeasurements: List<Measurement>
    ): List<Pair<Measurement, Measurement>> {
        // Tenta alinhar por timestamp
        val proximalByTime = proximalMeasurements.associateBy { it.capturedAt }
        val distalByTime = distalMeasurements.associateBy { it.capturedAt }

        val commonTimestamps = proximalByTime.keys.intersect(distalByTime.keys)

        if (commonTimestamps.isNotEmpty()) {
            return commonTimestamps.mapNotNull { time ->
                val proximal = proximalByTime[time]
                val distal = distalByTime[time]
                if (proximal != null && distal != null) Pair(proximal, distal) else null
            }
        }

        // Fallback: alinha por índice (readOrder)
        val proximalSorted = proximalMeasurements.sortedBy { it.readOrder ?: 0 }
        val distalSorted = distalMeasurements.sortedBy { it.readOrder ?: 0 }
        val minSize = minOf(proximalSorted.size, distalSorted.size)

        return (0 until minSize).map { i ->
            Pair(proximalSorted[i], distalSorted[i])
        }
    }
}

data class ROMComparison(
    val movement: MovementEnum,
    val leftROM: Double,
    val rightROM: Double,
    val difference: Double,
    val leftStats: AngleStatistics?,
    val rightStats: AngleStatistics?
) {
    val isSignificantDifference: Boolean
        get() = difference > 10.0 // >10° considerado significativo
}

// ============================================
// EXTENSÕES
// ============================================

fun Double.normalizeAngle(): Double {
    var normalized = this % 360.0
    if (normalized > 180.0) normalized -= 360.0
    if (normalized < -180.0) normalized += 360.0
    return normalized
}

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}