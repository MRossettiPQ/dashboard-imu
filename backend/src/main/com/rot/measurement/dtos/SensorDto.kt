package com.rot.measurement.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.measurement.models.Sensor
import java.util.*

open class SensorDto {
    var id: UUID? = null
    var ip: String? = null
    var macAddress: String? = null
    var sensorName: String? = null
    var observation: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: Sensor): SensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, SensorDto::class.java)
        }

        fun from(pagination: Pagination<Sensor>): Pagination<SensorDto> {
            return pagination.transform { from(it) }
        }
    }
}
