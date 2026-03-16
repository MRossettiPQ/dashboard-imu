package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.session.enums.NodeRoleEnum
import com.rot.session.models.MovementNode


class MovementNodeDto {
    var id: Int? = null
    var sessionNodeId: Int? = null
    var role: NodeRoleEnum? = null
    var movement: MovementDto? = null


    companion object {
        fun from(movement: MovementNode): MovementNodeDto {
            return MovementNodeDto().apply {
                this.id = movement.id
                this.role = movement.role
                this.sessionNodeId = movement.sessionNode?.id
                this.movement = MovementDto.from(movement.movement!!)
            }
        }

        fun from(pagination: Pagination<MovementNode>): Pagination<MovementNodeDto> {
            return pagination.transform { from(it) }
        }
    }
}