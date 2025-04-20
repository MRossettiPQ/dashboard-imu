package com.rot.access.dtos

import jakarta.validation.constraints.NotEmpty

class LoginDto {
    @field:NotEmpty(message = "Username is required")
    var username: String? = null
    @field:NotEmpty(message = "Password is required")
    var password: String? = null
}