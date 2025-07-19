package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.utils.JsonUtils
import com.rot.session.models.Movement
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*


open class BaseMovementDto {
    var id: UUID? = null
    var type: MovementTypeDto? = null
    var observation: String? = null
    var session: RetrieveSessionDto? = null
}

class RetrieveMovementDto: BaseMovementDto() {
    var sensors = mutableSetOf<RetrieveSensorDto>()

    companion object {
        fun from(entity: Movement): RetrieveMovementDto {
            return JsonUtils.MAPPER.convertValue(entity, RetrieveMovementDto::class.java)
        }
    }
}

class CreateMovementDto: BaseMovementDto() {
    var sensors = mutableSetOf<CreateSensorDto>()

    companion object {
        fun from(entity: Movement): CreateMovementDto {
            return JsonUtils.MAPPER.convertValue(entity, CreateMovementDto::class.java)
        }
    }
}


@Schema(description = "Resposta com dados do movimento realizado")
class MovementResponse : ContentDto<RetrieveMovementDto>()