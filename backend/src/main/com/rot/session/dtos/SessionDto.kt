package com.rot.session.dtos

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
           return SessionDto().apply {
               this.id = session.id
               this.date = session.date
               this.type = session.type
               this.procedure = session.procedure
               this.observation = session.observation
           }
        }
    }
}