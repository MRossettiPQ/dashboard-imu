package com.rot.session.dtos

import com.rot.core.jaxrs.ContentDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.session.models.Movement
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*


class MovementDto {
    var id: UUID? = null
    var type: MovementTypeDto? = null
    var observation: String? = null
    var session: SessionDto? = null
    var sensors = mutableSetOf<SensorDto>()

    companion object {
        fun from(entity: Movement): MovementDto {
            return JsonUtils.MAPPER.convertValue(entity, MovementDto::class.java)
        }
        fun from(paginationDto: PaginationDto<Movement>): PaginationDto<MovementDto> {
            return paginationDto.transform { from(it) }
        }
    }
}


@Schema(description = "Resposta com dados do movimento realizado")
class MovementResponse : ContentDto<MovementDto>()

@Schema(description = "Resposta com dados do paciente")
class MovementPaginationResponse : PaginationDto<MovementDto>()