package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.enums.MovementType
import com.rot.session.models.Movement
import java.util.*


class MovementDto {
    var id: UUID? = null
    var type: MovementType? = null
    var observation: String? = null
    var session: SessionDto? = null
    var sensors = mutableSetOf<SensorDto>()

    companion object {
        fun from(movement: Movement): MovementDto {
            return JsonUtils.MAPPER.convertValue(movement, MovementDto::class.java)
        }
    }
}
