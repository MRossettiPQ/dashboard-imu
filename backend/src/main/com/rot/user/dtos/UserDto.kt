package com.rot.user.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.rot.user.enums.UserRole
import com.rot.user.models.User
import java.util.*

class UserDto {
    var id: UUID? = null
    var active: Boolean = false
    var username: String? = null
    var name: String? = null
    var email: String? = null
    var role: UserRole = UserRole.PHYSIOTHERAPIST
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var token: String? = null

    companion object {
        fun from(user: User, token: Boolean = false): UserDto {
            val dto = UserDto()
            dto.id = user.id
            dto.active = user.active
            dto.username = user.username
            dto.name = user.name
            dto.email = user.email
            dto.role = user.role

            if (token) {
                dto.token = user.generateToken()
            }

            return dto
        }
    }
}