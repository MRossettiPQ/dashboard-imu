package com.rot.measurement.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.measurement.models.Sensor
import com.rot.session.enums.BodySegmentEnum
import java.util.*

open class SensorDto {
    var id: Int? = null
    var ip: String? = null
    var sensorInfo: SensorInfoDto? = null
    var bodySegment: BodySegmentEnum? = null
    var observation: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: Sensor): SensorDto {
            return SensorDto().apply {
                id = sensor.id
                ip = sensor.ip
                sensorInfo = SensorInfoDto.from(sensor.sensorInfo!!)
                bodySegment = sensor.bodySegment
                observation = sensor.observation
            }
        }

        fun from(pagination: Pagination<Sensor>): Pagination<SensorDto> {
            return pagination.transform { from(it) }
        }
    }
}
