package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.gonimetry.dtos.MovementTypeDto
import com.rot.measurement.dtos.SensorDto
import com.rot.session.models.Movement


class MovementDto {
    var id: Int? = null
    var type: MovementTypeDto? = null
    var observation: String? = null
    var session: SessionDto? = null
    var sensors = mutableSetOf<SensorDto>()

    companion object {
        fun from(entity: Movement): MovementDto {
            return JsonUtils.MAPPER.convertValue(entity, MovementDto::class.java)
        }
        fun from(pagination: Pagination<Movement>): Pagination<MovementDto> {
            return pagination.transform { from(it) }
        }
    }
}