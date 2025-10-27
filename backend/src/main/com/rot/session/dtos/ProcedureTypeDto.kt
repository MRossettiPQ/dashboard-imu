package com.rot.session.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.session.enums.ProcedureEnum
import com.rot.session.models.ProcedureType
import java.util.*


class ProcedureTypeDto {
    var id: UUID? = null
    var type: ProcedureEnum? = null
    var description: String? = null
    var movementsTypes = mutableSetOf<MovementTypeDto>()

    companion object {
        fun from(entity: ProcedureType) : ProcedureTypeDto {
            val dto = ProcedureTypeDto()
            dto.id = entity.id
            dto.type = entity.type
            dto.description = entity.description
            dto.movementsTypes = entity.movementsTypes.map { MovementTypeDto.from(it) }.toMutableSet()
            return dto
        }
        fun from(paginationDto: PaginationDto<ProcedureType>): PaginationDto<ProcedureTypeDto> {
            return paginationDto.transform { from(it) }
        }
    }
}
