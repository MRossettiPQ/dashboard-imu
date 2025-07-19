package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import com.rot.user.dtos.UserDto
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

open class BaseSessionDto {
    var id: UUID? = null
    var date: LocalDateTime? = null
    var type: SessionType = SessionType.REAL
    var observation: String? = null
    var patient: UserDto? = null
    var physiotherapist: UserDto? = null
}

class RetrieveSessionDto: BaseSessionDto() {
    var procedures = mutableSetOf<RetrieveProcedureDto>()

    companion object {
        fun from(entity: Session): RetrieveSessionDto {
            return JsonUtils.MAPPER.convertValue(entity, RetrieveSessionDto::class.java)
        }
    }
}

class CreateSessionDto: BaseSessionDto() {
    var procedures = mutableSetOf<CreateProcedureDto>()

    companion object {
        fun from(entity: Session): CreateSessionDto {
            return JsonUtils.MAPPER.convertValue(entity, CreateSessionDto::class.java)
        }
    }
}

@Schema(description = "Resposta com dados da sessão realizada")
class SessionResponse : ContentDto<RetrieveSessionDto>()