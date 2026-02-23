package com.rot.measurement.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.measurement.models.Measurement
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*


open class MeasurementDto {
    var id: UUID? = null
    var readOrder: Int = 0
    lateinit var capturedAt: OffsetDateTime
    lateinit var sensorName: String
    lateinit var accelMssX: BigDecimal
    lateinit var accelMssY: BigDecimal
    lateinit var accelMssZ: BigDecimal
    lateinit var accelLinX: BigDecimal
    lateinit var accelLinY: BigDecimal
    lateinit var accelLinZ: BigDecimal
    lateinit var gyroRadsX: BigDecimal
    lateinit var gyroRadsY: BigDecimal
    lateinit var gyroRadsZ: BigDecimal
    lateinit var roll: BigDecimal
    lateinit var pitch: BigDecimal
    lateinit var yaw: BigDecimal
    lateinit var eulerX: BigDecimal
    lateinit var eulerY: BigDecimal
    lateinit var eulerZ: BigDecimal
    lateinit var quaternionX: BigDecimal
    lateinit var quaternionY: BigDecimal
    lateinit var quaternionZ: BigDecimal
    lateinit var quaternionW: BigDecimal

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
        fun from(pagination: Pagination<Measurement>): Pagination<MeasurementDto> {
            return pagination.transform { from(it) }
        }
    }

    fun quaternion() = Quaternion(quaternionW, -quaternionX, -quaternionY, -quaternionZ)
}

data class Quaternion(
    val w: BigDecimal,
    val x: BigDecimal,
    val y: BigDecimal,
    val z: BigDecimal
)