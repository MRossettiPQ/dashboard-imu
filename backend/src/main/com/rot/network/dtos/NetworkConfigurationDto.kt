package com.rot.network.dtos

import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import com.rot.network.models.NetworkConfiguration
import jakarta.validation.constraints.NotEmpty
import java.util.*

class NetworkConfigurationDto {
    var id: UUID? = null
    var mdns: String? = null
    var ip: String? = null

    @field:NotEmpty(message = "Ssid is required")
    var ssid: String? = null

    @field:NotEmpty(message = "Password is required")
    var password: String? = null

    companion object {
        fun fromDecrypted(entity: NetworkConfiguration, decryptPassword: Boolean = false): NetworkConfigurationDto {
            val dto = JsonUtils.MAPPER.convertValue(entity, NetworkConfigurationDto::class.java)
            dto.password = entity.decryptedPassword
            return dto
        }
        fun from(entity: NetworkConfiguration, decryptPassword: Boolean = false): NetworkConfigurationDto {
            return JsonUtils.MAPPER.convertValue(entity, NetworkConfigurationDto::class.java)
        }
        fun from(paginationDto: PaginationDto<NetworkConfiguration>) : PaginationDto<NetworkConfigurationDto> {
            return paginationDto.transform { from(it) }
        }
    }
}