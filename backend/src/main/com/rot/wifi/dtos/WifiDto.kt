package com.rot.wifi.dtos

import com.rot.wifi.models.Wifi
import java.util.*

class WifiDto {
    var id: UUID? = null
    var ssid: String? = null
    var password: String? = null

    companion object {
        fun from(wifi: Wifi, decryptPassword: Boolean = false): WifiDto {
            val dto = WifiDto()
            dto.id = wifi.id
            dto.ssid = wifi.ssid
            if (decryptPassword) {
                dto.password = wifi.decryptedPassword
            }
            return dto
        }
    }
}