package com.rot.session.services

import com.rot.file.models.FileStorage
import com.rot.session.dtos.ArticulationTypeCreateOrUpgradeDto
import com.rot.session.models.ArticulationType
import com.rot.session.models.MovementType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Valid

@ApplicationScoped
class ArticulationTypeService {

    fun save(@Valid body: ArticulationTypeCreateOrUpgradeDto): ArticulationType {
        val articulationType = ArticulationType.findOrCreateInstance(body.id)
        articulationType.type = body.type
        articulationType.description = body.description

        val movements: MutableSet<MovementType> = mutableSetOf()
        for (typeDto in body.movementsTypes) {
            val movementType = MovementType.findOrCreateInstance(typeDto.id)
            movementType.type = typeDto.type
            movementType.description = typeDto.description
            movementType.image = FileStorage.findOrThrowById(typeDto.image!!.id!!)
            movementType.angleRule = typeDto.angleRule

            movements.add(movementType)
        }

        return articulationType.save()
    }


}