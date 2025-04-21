package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.enums.PositionEnum
import com.rot.session.enums.SensorType
import com.rot.session.models.Sensor
import java.util.*


class SensorDto {
    var id: UUID? = null
    var macAddress: String? = null
    var sensorName: String? = null
    var position: PositionEnum? = null
    var type: SensorType = SensorType.GYROSCOPE
    var observation: String? = null
    var movement: MovementDto? = null
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: Sensor): SensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, SensorDto::class.java)
        }
    }
}
