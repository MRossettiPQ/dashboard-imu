package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.session.enums.BodyRegionEnum
import com.rot.session.models.Session
import com.rot.session.models.SessionNode

open class SessionNodeBase {
    var id: Int? = null
    var observation: String? = null
    lateinit var region: BodyRegionEnum
}

open class SessionNodeRead: SessionNodeBase() {
    var session: Session? = null
    var nodeSensors = mutableSetOf<NodeSensorRead>()

    companion object {
        fun from(entity: SessionNode): SessionNodeRead {
            return SessionNodeRead().apply {
                id = entity.id
                observation = entity.observation
                session = entity.session
                nodeSensors = entity.nodeSensors.map { NodeSensorRead.from(it) }.toMutableSet()
            }
        }

        fun from(pagination: Pagination<SessionNode>): Pagination<SessionNodeRead> {
            return pagination.transform { from(it) }
        }
    }
}

open class SessionNodeCreateOrUpdate {
    lateinit var region: BodyRegionEnum
    var sensorMacs: Map<String, String> = emptyMap()
}