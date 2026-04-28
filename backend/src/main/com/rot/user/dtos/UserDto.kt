package com.rot.user.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.rot.access.dtos.AccessDto
import com.rot.core.jaxrs.Pagination
import com.rot.core.utils.JsonUtils
import com.rot.user.enums.UserRole
import com.rot.user.models.User
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.util.*

@Schema(name = "UserDto", description = "Dados do usuário")
class UserDto {
    var id: UUID? = null
    var active: Boolean = false
    lateinit var username: String
    lateinit var email: String
    lateinit var name: String
    var role: UserRole = UserRole.PHYSIOTHERAPIST
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var access: AccessDto? = null
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var password: String? = null

    companion object {
        fun from(entity: User, token: Boolean = false): UserDto {
            val dto = JsonUtils.MAPPER.convertValue(entity, UserDto::class.java)

            if (token) {
                dto.access = entity.access
            }

            return dto
        }
        fun from(pagination: Pagination<User>) : Pagination<UserDto> {
            return pagination.transform { from(it) }
        }
    }
}