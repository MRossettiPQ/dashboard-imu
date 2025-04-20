package com.rot.wifi.dtos

import jakarta.validation.constraints.NotEmpty

class CreateWifiDto {
    @field:NotEmpty(message = "Ssid is required")
    var ssid: String? = null

    @field:NotEmpty(message = "Password is required")
    var password: String? = null
}