package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.gonimetry.dtos.MovementTypeDto
import com.rot.gonimetry.enums.MovementEnum
import com.rot.session.models.Movement


open class MovementBase {
    var id: Int? = null
    var type: MovementEnum? = null
    var movementType: MovementTypeDto? = null
    var observation: String? = null

    companion object {
        fun from(movement: Movement): MovementRead {
            return MovementRead().apply {
                id = movement.id
                type = movement.type
                movementType = MovementTypeDto.from(movement.movementType!!)
            }
        }
        fun from(pagination: Pagination<Movement>): Pagination<MovementRead> {
            return pagination.transform { from(it) }
        }
    }
}


class MovementRead: MovementBase() {
    companion object {
        fun from(movement: Movement): MovementRead {
            return MovementRead().apply {
                id = movement.id
                type = movement.type
                observation = movement.observation
                movementType = MovementTypeDto.from(movement.movementType!!)
            }
        }
        fun from(pagination: Pagination<Movement>): Pagination<MovementRead> {
            return pagination.transform { from(it) }
        }
    }
}