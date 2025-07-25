package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.models.Measurement
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


open class BaseMeasurementDto {
    var id: UUID? = null
    var capturedAt: LocalDateTime? = null
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
}

class CreateMeasurementDto: BaseMeasurementDto() {
    companion object {
        fun from(measurement: Measurement) : CreateMeasurementDto {
            return JsonUtils.MAPPER.convertValue(measurement, CreateMeasurementDto::class.java)
        }
    }
}

class RetrieveMeasurementDto: BaseMeasurementDto() {
    companion object {
        fun from(measurement: Measurement) : RetrieveMeasurementDto {
            return JsonUtils.MAPPER.convertValue(measurement, RetrieveMeasurementDto::class.java)
        }
    }
}
