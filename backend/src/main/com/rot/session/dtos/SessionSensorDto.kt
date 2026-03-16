package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.measurement.dtos.MeasurementDto
import com.rot.measurement.dtos.SensorInfoDto
import com.rot.session.models.SessionSensor

open class SessionSensorDto {
    var id: Int? = null
    var ip: String? = null
    var observation: String? = null
    var sensorInfo: SensorInfoDto? = null
    var nodeSensors = mutableSetOf<NodeSensorDto>()
    var measurements = mutableSetOf<MeasurementDto>()

    companion object {
        fun from(entity: SessionSensor): SessionSensorDto {
            return SessionSensorDto().apply {
                id = entity.id
                ip = entity.ip
                observation = entity.observation
                sensorInfo = SensorInfoDto.from(entity.sensorInfo!!)
                nodeSensors = entity.nodeSensors.map { NodeSensorDto.from(it) }.toMutableSet()
                measurements = entity.measurements.map { MeasurementDto.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<SessionSensor>): Pagination<SessionSensorDto> {
            return pagination.transform { from(it) }
        }
    }
}