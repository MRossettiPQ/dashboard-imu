package com.rot.measurement.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.measurement.models.SensorInfo

open class SensorInfoBase {
    var id: Int? = null
    var macAddress: String? = null
    var sensorName: String? = null

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SensorInfoBase) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return macAddress?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }
}


class SensorInfoRead : SensorInfoBase() {
    companion object {
        fun from(sensor: SensorInfo): SensorInfoRead {
            return SensorInfoRead().apply {
                id = sensor.id
                macAddress = sensor.macAddress
                sensorName = sensor.sensorName
            }
        }

        fun from(pagination: Pagination<SensorInfo>): Pagination<SensorInfoRead> {
            return pagination.transform { from(it) }
        }
    }
}