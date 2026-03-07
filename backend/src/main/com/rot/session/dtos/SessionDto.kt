package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import com.rot.user.dtos.UserDto
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

open class SessionDto {
    var id: UUID? = null
    var sessionDate: OffsetDateTime? = null
    var type: SessionType = SessionType.REAL
    var observation: String? = null
    var patient: UserDto? = null
    var physiotherapist: UserDto? = null
    var articulations = mutableSetOf<ArticulationDto>()

    companion object {
        fun from(entity: Session): SessionDto {
            return SessionDto().apply {
                id = entity.id
                sessionDate = entity.sessionDate
                type = entity.type
                observation = entity.observation
                patient = UserDto.from(entity.patient!!)
                physiotherapist = UserDto.from(entity.physiotherapist!!)
                articulations = entity.articulations.map { ArticulationDto.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<Session>): Pagination<SessionDto> {
            return pagination.transform { from(it) }
        }
    }
}