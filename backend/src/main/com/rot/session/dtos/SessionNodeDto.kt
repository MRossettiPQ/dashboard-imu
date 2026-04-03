package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.BodySideEnum
import com.rot.session.enums.SessionType
import com.rot.session.models.NodeSensor
import com.rot.session.models.Session
import com.rot.session.models.SessionNode
import com.rot.session.models.SessionSensor
import com.rot.user.dtos.UserDto
import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

open class SessionNodeDto {
    var id: Int? = null
    var observation: String? = null
    var side: BodySideEnum = BodySideEnum.RIGHT
    var session: Session? = null
    var nodeSensors = mutableSetOf<NodeSensorDto>()

    companion object {
        fun from(entity: SessionNode): SessionNodeDto {
            return SessionNodeDto().apply {
                id = entity.id
                observation = entity.observation
                side = entity.side
                session = entity.session
                nodeSensors = entity.nodeSensors.map { NodeSensorDto.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<SessionNode>): Pagination<SessionNodeDto> {
            return pagination.transform { from(it) }
        }
    }
}