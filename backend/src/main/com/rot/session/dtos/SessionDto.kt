package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import com.rot.session.models.SessionNode
import com.rot.session.models.SessionSensor
import com.rot.user.dtos.UserDto
import jakarta.persistence.CascadeType
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
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
    var sessionSensors = mutableSetOf<SessionSensorDto>()
    var sessionNodes = mutableSetOf<SessionNodeDto>()

    companion object {
        fun from(entity: Session): SessionDto {
            return SessionDto().apply {
                id = entity.id
                sessionDate = entity.sessionDate
                type = entity.type
                observation = entity.observation
                patient = UserDto.from(entity.patient!!)
                physiotherapist = UserDto.from(entity.physiotherapist!!)
                sessionSensors = entity.sessionSensors.map { SessionSensorDto.from(it) }.toMutableSet()
                sessionNodes = entity.sessionNodes.map { SessionNodeDto.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<Session>): Pagination<SessionDto> {
            return pagination.transform { from(it) }
        }
    }
}

open class CreateSessionDto {
    lateinit var patientId: UUID
    var type: SessionType = SessionType.REAL
    var observation: String? = null
}