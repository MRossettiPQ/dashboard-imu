package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.ArticulationEnum
import com.rot.session.models.Articulation
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*

open class ArticulationDto {
    var id: UUID? = null
    var type: ArticulationEnum? = null

    companion object {
        fun from(entity: Articulation): ArticulationDto {
            return JsonUtils.MAPPER.convertValue(entity, ArticulationDto::class.java)
        }
        fun from(paginationDto: PaginationDto<Articulation>): PaginationDto<ArticulationDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados do procedimento")
class ProcedureResponse : ContentDto<ArticulationDto>()

@Schema(description = "Resposta com dados paginados do procedimento realizado")
class ProcedurePaginationResponse : PaginationDto<ArticulationDto>()