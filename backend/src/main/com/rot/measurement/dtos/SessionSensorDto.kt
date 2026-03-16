package com.rot.measurement.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.measurement.dtos.SessionSensorDto.Companion.from
import com.rot.session.models.SessionSensor
import java.util.*

open class SessionSensorDto {
    var id: Int? = null
    var ip: String? = null
    var sensorInfo: SensorInfoDto? = null
    var observation: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: SessionSensor): SessionSensorDto {
            return SessionSensorDto().apply {
                id = sensor.id
                ip = sensor.ip
                sensorInfo = SensorInfoDto.from(sensor.sensorInfo!!)
                observation = sensor.observation
            }
        }

        fun from(pagination: Pagination<SessionSensor>): Pagination<SessionSensorDto> {
            return pagination.transform { from(it) }
        }
    }
}
