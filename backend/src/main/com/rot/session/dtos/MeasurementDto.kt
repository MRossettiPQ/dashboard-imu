package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.models.Measurement
import com.rot.socket.dtos.SessionSensorDto
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*


open class MeasurementDto {
    var id: UUID? = null
    var capturedAt: OffsetDateTime? = null
    var sensorName: String? = null
    var readOrder: Int? = null
    var accelMssX: BigDecimal? = null
    var accelMssY: BigDecimal? = null
    var accelMssZ: BigDecimal? = null
    var accelLinX: BigDecimal? = null
    var accelLinY: BigDecimal? = null
    var accelLinZ: BigDecimal? = null
    var gyroRadsX: BigDecimal? = null
    var gyroRadsY: BigDecimal? = null
    var gyroRadsZ: BigDecimal? = null
    var roll: BigDecimal? = null
    var pitch: BigDecimal? = null
    var yaw: BigDecimal? = null
    var eulerX: BigDecimal? = null
    var eulerY: BigDecimal? = null
    var eulerZ: BigDecimal? = null
    var quaternionX: BigDecimal? = null
    var quaternionY: BigDecimal? = null
    var quaternionZ: BigDecimal? = null
    var quaternionW: BigDecimal? = null

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MeasurementDto) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }

    companion object {
        fun from(measurement: Measurement) : MeasurementDto {
            return JsonUtils.MAPPER.convertValue(measurement, MeasurementDto::class.java)
        }
        fun from(paginationDto: PaginationDto<Measurement>): PaginationDto<MeasurementDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados do paciente")
class MeasurementResponse : ContentDto<MeasurementDto>()

@Schema(description = "Resposta com dados do paciente")
class MeasurementPaginationResponse : PaginationDto<MeasurementDto>()