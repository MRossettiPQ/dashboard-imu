package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.gonimetry.enums.ArticulationEnum
import com.rot.session.models.Articulation

open class ArticulationDto {
    var id: Int? = null
    var type: ArticulationEnum? = null

    companion object {
        fun from(entity: Articulation): ArticulationDto {
            return JsonUtils.MAPPER.convertValue(entity, ArticulationDto::class.java)
        }
        fun from(pagination: Pagination<Articulation>): Pagination<ArticulationDto> {
            return pagination.transform { from(it) }
        }
    }
}