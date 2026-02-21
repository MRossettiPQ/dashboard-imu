package com.rot.session.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.BodySegmentEnum
import com.rot.session.enums.JointEnum
import com.rot.session.enums.SensorOrientationEnum
import com.rot.session.enums.SensorPlacementEnum
import com.rot.session.models.Sensor
import java.util.*

open class SensorDto {
    var id: UUID? = null
    var ip: String? = null
    var macAddress: String? = null
    var sensorName: String? = null
    var position: PositionEnum? = null
    var type: SensorType = SensorType.GYROSCOPE
    var observation: String? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(sensor: Sensor): SensorDto {
            return JsonUtils.MAPPER.convertValue(sensor, SensorDto::class.java)
        }

        fun from(paginationDto: PaginationDto<Sensor>): PaginationDto<SensorDto> {
            return paginationDto.transform { from(it) }
        }
    }
}
