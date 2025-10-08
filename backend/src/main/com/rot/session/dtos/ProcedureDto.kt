package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.ProcedureType
import com.rot.session.models.Procedure
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*

open class ProcedureDto {
    var id: UUID? = null
    var type: ProcedureType? = null

    companion object {
        fun from(entity: Procedure): ProcedureDto {
            return JsonUtils.MAPPER.convertValue(entity, ProcedureDto::class.java)
        }
        fun from(paginationDto: PaginationDto<Procedure>): PaginationDto<ProcedureDto> {
            return paginationDto.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados do procedimento")
class ProcedureResponse : ContentDto<ProcedureDto>()

@Schema(description = "Resposta com dados paginados do procedimento realizado")
class ProcedurePaginationResponse : PaginationDto<ProcedureDto>()