package com.rot.session.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.MovementEnum
import com.rot.session.models.MovementType
import java.util.*


class MovementTypeDto {
    var id: UUID? = null
    var type: MovementEnum? = null

    companion object {
        fun from(measurement: MovementType) : MovementTypeDto {
            return JsonUtils.MAPPER.convertValue(measurement, MovementTypeDto::class.java)
        }
        fun from(paginationDto: PaginationDto<MovementType>): PaginationDto<MovementTypeDto> {
            return paginationDto.transform { from(it) }
        }
    }
}
