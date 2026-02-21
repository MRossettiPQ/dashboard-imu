package com.rot.session.dtos

import FileStorageDto
import com.rot.core.jaxrs.PaginationDto
import com.rot.session.enums.MovementEnum
import com.rot.session.models.AngleRule
import com.rot.session.models.MovementType
import jakarta.validation.constraints.NotNull


class MovementTypeDto {
    var id: Int? = null
    var articulationId: Int? = null
    var type: MovementEnum? = null
    var description: String? = null
    var image: FileStorageDto? = null
    var angleRule: AngleRule? = null

    companion object {
        fun from(entity: MovementType) : MovementTypeDto {
            val dto = MovementTypeDto()
            dto.id = entity.id
            dto.type = entity.type
            dto.description = entity.description
            dto.image = FileStorageDto.from(entity.image!!)
            dto.angleRule = entity.angleRule
            dto.articulationId = entity.articulationType?.id
            return dto
        }
    }
}


class MovementTypeCreateOrUpgradeDto {
    var id: Int? = null

    @NotNull
    var type: MovementEnum? = null

    @NotNull
    var description: String? = null

    @NotNull
    var image: FileStorageDto? = null

    @NotNull
    var angleRule: AngleRule? = null
}
