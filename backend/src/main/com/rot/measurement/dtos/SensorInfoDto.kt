package com.rot.measurement.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.measurement.models.SensorInfo
import java.util.*

open class SensorInfoDto {
    var id: Int? = null
    var macAddress: String? = null
    var sensorName: String? = null

    companion object {
        fun from(sensor: SensorInfo): SensorInfoDto {
            return SensorInfoDto().apply {
                macAddress = sensor.macAddress
                sensorName = sensor.sensorName
            }
        }

        fun from(pagination: Pagination<SensorInfo>): Pagination<SensorInfoDto> {
            return pagination.transform { from(it) }
        }
    }
}
