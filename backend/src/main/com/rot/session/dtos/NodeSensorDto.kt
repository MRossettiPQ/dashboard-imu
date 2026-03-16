package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.session.enums.SessionType
import com.rot.session.models.NodeSensor
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

open class NodeSensorDto {
    var id: Int? = null
    var sessionSensorId: Int? = null
    var sessionNode: SessionNodeDto? = null


    companion object {
        fun from(entity: NodeSensor): NodeSensorDto {
            return NodeSensorDto().apply {
                id = entity.id
                sessionSensorId = entity.sessionSensor?.id
                sessionNode = SessionNodeDto.from(entity.sessionNode!!)
            }
        }

        fun from(pagination: Pagination<NodeSensor>): Pagination<NodeSensorDto> {
            return pagination.transform { from(it) }
        }
    }
}