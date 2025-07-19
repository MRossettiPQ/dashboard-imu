package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.ProcedureType
import com.rot.session.models.Procedure
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*

open class BaseProcedureDto {
    var id: UUID? = null
    var type: ProcedureType? = null
}

class RetrieveProcedureDto: BaseProcedureDto() {
    var movements = mutableSetOf<RetrieveMovementDto>()

    companion object {
        fun from(entity: Procedure): RetrieveProcedureDto {
            return JsonUtils.MAPPER.convertValue(entity, RetrieveProcedureDto::class.java)
        }
    }
}

class CreateProcedureDto: BaseProcedureDto() {
    var movements = mutableSetOf<CreateMovementDto>()

    companion object {
        fun from(entity: Procedure): CreateProcedureDto {
            return JsonUtils.MAPPER.convertValue(entity, CreateProcedureDto::class.java)
        }
    }
}

@Schema(description = "Resposta com dados do procedimento")
class ProcedureResponse : ContentDto<RetrieveProcedureDto>()