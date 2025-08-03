package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import com.rot.user.dtos.UserDto
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

open class SessionDto {
    var id: UUID? = null
    var date: LocalDateTime? = null
    var type: SessionType = SessionType.REAL
    var observation: String? = null
    var patient: UserDto? = null
    var physiotherapist: UserDto? = null
    var procedures = mutableSetOf<ProcedureDto>()

    companion object {
        fun from(entity: Session): SessionDto {
            return JsonUtils.MAPPER.convertValue(entity, SessionDto::class.java)
        }

        fun from(pagination: Pagination<Session>): Pagination<SessionDto> {
            return pagination.transform { from(it) }
        }
    }
}

@Schema(description = "Resposta com dados da sessão realizada")
class SessionResponse : ContentDto<SessionDto>()