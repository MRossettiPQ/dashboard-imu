package com.rot.session.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.session.enums.ArticulationEnum
import com.rot.session.models.ArticulationType
import jakarta.validation.constraints.NotNull


class ArticulationTypeDto {
    var id: Int? = null
    var type: ArticulationEnum? = null
    var description: String? = null
    var movementsTypes = mutableSetOf<MovementTypeDto>()

    companion object {
        fun from(entity: ArticulationType): ArticulationTypeDto {
            val dto = ArticulationTypeDto()
            dto.id = entity.id
            dto.type = entity.type
            dto.description = entity.description
            dto.movementsTypes = entity.movementsTypes.map { MovementTypeDto.from(it) }.toMutableSet()
            return dto
        }

        fun from(paginationDto: PaginationDto<ArticulationType>): PaginationDto<ArticulationTypeDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

class ArticulationTypeCreateOrUpgradeDto {
    var id: Int? = null

    @NotNull
    var type: ArticulationEnum? = null

    @NotNull
    var description: String? = null
    var movementsTypes = mutableSetOf<MovementTypeCreateOrUpgradeDto>()
}