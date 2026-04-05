package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.session.enums.SessionType
import com.rot.session.models.Session
import com.rot.user.dtos.UserDto
import java.time.OffsetDateTime
import java.util.*

open class SessionBase {
    var id: UUID? = null
    var sessionDate: OffsetDateTime? = null
    open var type: SessionType = SessionType.REAL
    open var observation: String? = null
    var patient: UserDto? = null
    var physiotherapist: UserDto? = null
    var sessionSensors = mutableSetOf<SessionSensorRead>()
    var sessionNodes = mutableSetOf<SessionNodeRead>()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SessionBase) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }
}

open class SessionRead: SessionBase() {
    companion object {
        fun from(entity: Session): SessionRead {
            return SessionRead().apply {
                id = entity.id
                sessionDate = entity.sessionDate
                type = entity.type
                observation = entity.observation
                patient = UserDto.from(entity.patient!!)
                physiotherapist = UserDto.from(entity.physiotherapist!!)
                sessionSensors = entity.sessionSensors.map { SessionSensorRead.from(it) }.toMutableSet()
                sessionNodes = entity.sessionNodes.map { SessionNodeRead.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<Session>): Pagination<SessionRead> {
            return pagination.transform { from(it) }
        }
    }
}

open class SessionCreateOrUpdate {
    var id: UUID? = null
    lateinit var patientId: UUID
    var type: SessionType = SessionType.REAL
    var observation: String? = null

    var sessionNodes = mutableSetOf<SessionNodeCreateOrUpdate>()
    var sessionSensors = mutableSetOf<SessionSensorCreateOrUpdate>()
}