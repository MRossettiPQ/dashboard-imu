package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.gonimetry.dtos.ArticulationTypeDto
import com.rot.gonimetry.enums.ArticulationEnum
import com.rot.gonimetry.models.ArticulationType
import com.rot.session.models.Articulation

open class ArticulationDto {
    var id: Int? = null
    var type: ArticulationTypeDto? = null
    var description: String? = null
    var movements = mutableSetOf<MovementDto>()

    companion object {
        fun from(entity: Articulation): ArticulationDto {
            return ArticulationDto().apply {
                id = entity.id
                type = ArticulationTypeDto.from(entity.type!!)
                description = entity.description
                movements = entity.movements.map { MovementDto.from(it) }.toMutableSet()
            }
        }
        fun from(pagination: Pagination<Articulation>): Pagination<ArticulationDto> {
            return pagination.transform { from(it) }
        }
    }
}