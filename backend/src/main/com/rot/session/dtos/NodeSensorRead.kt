package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.session.enums.BodySegmentationEnum
import com.rot.session.models.NodeSensor

open class NodeSensorBase {
    var id: Int? = null
    var sessionSensorId: Int? = null
    var sessionNode: SessionNodeRead? = null
    lateinit var segmentation: BodySegmentationEnum
}

open class NodeSensorRead: NodeSensorBase() {
    companion object {
        fun from(entity: NodeSensor): NodeSensorRead {
            return NodeSensorRead().apply {
                id = entity.id
                sessionSensorId = entity.sessionSensor?.id
                sessionNode = SessionNodeRead.from(entity.sessionNode!!)
            }
        }

        fun from(pagination: Pagination<NodeSensor>): Pagination<NodeSensorRead> {
            return pagination.transform { from(it) }
        }
    }
}