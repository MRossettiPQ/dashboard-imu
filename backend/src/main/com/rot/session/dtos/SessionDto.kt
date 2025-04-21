package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.enums.ProcedureType
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import java.time.LocalDateTime
import java.util.*

class SessionDto {
    var id: UUID? = null
    var date: LocalDateTime? = null
    var type: SessionType? = null
    var procedure: ProcedureType? = null
    var observation: String? = null

    companion object {
        fun from(session: Session): SessionDto {
            return JsonUtils.MAPPER.convertValue(session, SessionDto::class.java)
        }
    }
}