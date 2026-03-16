package com.rot.session.dtos

import com.rot.core.jaxrs.Pagination
import com.rot.gonimetry.models.BodyPoint


class BodyPointDto {
    var id: Int? = null
    var name: String? = null
    var description: String? = null
    var region: String? = null


    companion object {
        fun from(movement: BodyPoint): BodyPointDto {
            return BodyPointDto().apply {
                id = movement.id
                name = movement.name
                description = movement.description
                region = movement.region
            }
        }
        fun from(pagination: Pagination<BodyPoint>): Pagination<BodyPointDto> {
            return pagination.transform { from(it) }
        }
    }
}