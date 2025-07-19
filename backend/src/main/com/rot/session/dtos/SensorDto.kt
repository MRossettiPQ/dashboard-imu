package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.enums.PositionEnum
import com.rot.session.enums.SensorType
import com.rot.session.models.Sensor
import java.util.*

open class BaseSensorDto {
    var id: UUID? = null
    var ip: String? = null
    var macAddress: String? = null
    var sensorName: String? = null
    var position: PositionEnum? = null
    var type: SensorType = SensorType.GYROSCOPE
    var observation: String? = null
    var movement: RetrieveMovementDto? = null
}

class RetrieveSensorDto: BaseSensorDto() {
    companion object {
        fun from(sensor: Sensor): RetrieveSensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, RetrieveSensorDto::class.java)
        }
    }
}

class CreateSensorDto: BaseSensorDto() {
    var measurements = mutableSetOf<CreateMeasurementDto>()

    companion object {
        fun from(sensor: Sensor): CreateSensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, CreateSensorDto::class.java)
        }
    }
}
