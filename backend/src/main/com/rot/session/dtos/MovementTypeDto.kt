package com.rot.session.dtos

import com.rot.core.utils.JsonUtils
import com.rot.session.enums.MovementEnum
import com.rot.session.models.Movement
import com.rot.session.models.MovementType
import java.util.*


class MovementTypeDto {
    var id: UUID? = null
    var type: MovementEnum? = null

    companion object {
        fun from(entity: MovementType): MovementTypeDto {
            return JsonUtils.MAPPER.convertValue(entity, MovementTypeDto::class.java)
        }
    }
}
