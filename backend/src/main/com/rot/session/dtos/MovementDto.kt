package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.gonimetry.dtos.MovementTypeDto
import com.rot.gonimetry.enums.MovementEnum
import com.rot.gonimetry.models.MovementType
import com.rot.measurement.dtos.SensorDto
import com.rot.session.models.Movement


class MovementDto {
    var id: Int? = null
    var type: MovementEnum? = null
    var movementType: MovementTypeDto? = null
    var observation: String? = null
    var sensors = mutableSetOf<SensorDto>()

    companion object {
        fun from(movement: Movement): MovementDto {
            return MovementDto().apply {
                id = movement.id
                type = movement.type
                movementType = MovementTypeDto.from(movement.movementType!!)
                sensors = movement.sensors.map { SensorDto.from(it) }.toMutableSet()
            }
        }
        fun from(pagination: Pagination<Movement>): Pagination<MovementDto> {
            return pagination.transform { from(it) }
        }
    }
}