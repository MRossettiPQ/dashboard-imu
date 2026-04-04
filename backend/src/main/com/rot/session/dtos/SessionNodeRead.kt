package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.session.enums.BodySideEnum
import com.rot.session.models.Session
import com.rot.session.models.SessionNode

open class SessionNodeBase {
    var id: Int? = null
    var observation: String? = null
    var side: BodySideEnum = BodySideEnum.RIGHT
    var session: Session? = null
    var nodeSensors = mutableSetOf<NodeSensorRead>()
}

open class SessionNodeRead: SessionNodeBase() {
    companion object {
        fun from(entity: SessionNode): SessionNodeRead {
            return SessionNodeRead().apply {
                id = entity.id
                observation = entity.observation
                side = entity.side
                session = entity.session
                nodeSensors = entity.nodeSensors.map { NodeSensorRead.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<SessionNode>): Pagination<SessionNodeRead> {
            return pagination.transform { from(it) }
        }
    }
}