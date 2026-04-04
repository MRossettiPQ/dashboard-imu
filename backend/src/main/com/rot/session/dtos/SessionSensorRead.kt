package com.rot.session.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.core.jaxrs.Pagination
import com.rot.measurement.dtos.MeasurementRead
import com.rot.measurement.dtos.SensorInfoRead
import com.rot.session.models.SessionSensor
import java.util.UUID


open class SessionSensorBase {
    open var id: Int? = null
    var ip: String? = null
    var observation: String? = null
    var sessionId: UUID? = null

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SessionSensorBase) return false
        return ip == other.ip
    }

    override fun hashCode(): Int {
        return ip?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[ip: $ip]"
    }
}

open class SessionSensorRead: SessionSensorBase() {
    var sensorInfo: SensorInfoRead? = null
    var nodeSensors = mutableSetOf<NodeSensorRead>()

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var measurements = mutableSetOf<MeasurementRead>()

    companion object {
        fun from(entity: SessionSensor): SessionSensorRead {
            return SessionSensorRead().apply {
                id = entity.id
                ip = entity.ip
                sessionId = entity.session?.id
                observation = entity.observation
                sensorInfo = SensorInfoRead.from(entity.sensorInfo!!)
                nodeSensors = entity.nodeSensors.map { NodeSensorRead.from(it) }.toMutableSet()
                measurements = entity.measurements.map { MeasurementRead.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<SessionSensor>): Pagination<SessionSensorRead> {
            return pagination.transform { from(it) }
        }
    }
}