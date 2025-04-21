package com.rot.network.dtos

import com.rot.network.models.NetworkConfiguration
import java.util.*

class NetworkConfigurationDto {
    var id: UUID? = null
    var mdns: String? = null
    var ip: String? = null
    var ssid: String? = null
    var password: String? = null

    companion object {
        fun from(networkConfiguration: NetworkConfiguration, decryptPassword: Boolean = false): NetworkConfigurationDto {
            val dto = NetworkConfigurationDto()
            dto.id = networkConfiguration.id
            dto.ssid = networkConfiguration.ssid
            if (decryptPassword) {
                dto.password = networkConfiguration.decryptedPassword
            }
            return dto
        }
    }
}