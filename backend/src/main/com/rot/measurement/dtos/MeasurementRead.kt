package com.rot.measurement.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.measurement.models.Measurement
import java.time.OffsetDateTime
import java.util.*
import kotlin.properties.Delegates


open class MeasurementBase {
    var id: UUID? = null
    var readOrder: Int = 0
    lateinit var capturedAt: OffsetDateTime
    lateinit var sensorName: String
    var accelX by Delegates.notNull<Double>()
    var accelY by Delegates.notNull<Double>()
    var accelZ by Delegates.notNull<Double>()
    var accelMssX by Delegates.notNull<Double>()
    var accelMssY by Delegates.notNull<Double>()
    var accelMssZ by Delegates.notNull<Double>()
    var accelLinX by Delegates.notNull<Double>()
    var accelLinY by Delegates.notNull<Double>()
    var accelLinZ by Delegates.notNull<Double>()
    var gyroRadsX by Delegates.notNull<Double>()
    var gyroRadsY by Delegates.notNull<Double>()
    var gyroRadsZ by Delegates.notNull<Double>()
    var magX by Delegates.notNull<Double>()
    var magY by Delegates.notNull<Double>()
    var magZ by Delegates.notNull<Double>()
    var roll by Delegates.notNull<Double>()
    var pitch by Delegates.notNull<Double>()
    var yaw by Delegates.notNull<Double>()
    var eulerX by Delegates.notNull<Double>()
    var eulerY by Delegates.notNull<Double>()
    var eulerZ by Delegates.notNull<Double>()
    var quaternionX by Delegates.notNull<Double>()
    var quaternionY by Delegates.notNull<Double>()
    var quaternionZ by Delegates.notNull<Double>()
    var quaternionW by Delegates.notNull<Double>()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MeasurementRead) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }

    companion object {
        fun from(measurement: Measurement): MeasurementRead {
            return JsonUtils.MAPPER.convertValue(measurement, MeasurementRead::class.java)
        }

        fun from(pagination: Pagination<Measurement>): Pagination<MeasurementRead> {
            return pagination.transform { from(it) }
        }
    }

    fun quaternion() = Quaternion(quaternionW, -quaternionX, -quaternionY, -quaternionZ)
}

open class MeasurementRead : MeasurementBase() {
    companion object {
        fun from(measurement: Measurement): MeasurementRead {
            return JsonUtils.MAPPER.convertValue(measurement, MeasurementRead::class.java)
        }

        fun from(pagination: Pagination<Measurement>): Pagination<MeasurementRead> {
            return pagination.transform { from(it) }
        }
    }
}

data class Quaternion(
    val w: Double,
    val x: Double,
    val y: Double,
    val z: Double
)