package com.rot.session.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.session.enums.MovementEnum
import com.rot.session.models.AngleRule
import com.rot.session.models.MovementType
import java.util.*


class MovementTypeDto {
    var id: UUID? = null
    var type: MovementEnum? = null
    var description: String? = null
    var imageName: String? = null
    var angleRule: AngleRule? = null
    companion object {
        fun from(entity: MovementType) : MovementTypeDto {
            val dto = MovementTypeDto()
            dto.id = entity.id
            dto.type = entity.type
            dto.description = entity.description
            dto.imageName = entity.imageName
            dto.angleRule = entity.angleRule
            return dto
        }
        fun from(paginationDto: PaginationDto<MovementType>): PaginationDto<MovementTypeDto> {
            return paginationDto.transform { from(it) }
        }
    }
}
