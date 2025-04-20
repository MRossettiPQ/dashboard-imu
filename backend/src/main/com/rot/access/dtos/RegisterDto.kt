package com.rot.access.dtos

import jakarta.validation.constraints.NotEmpty

class RegisterDto {
    @field:NotEmpty(message = "Username is required")
    var username: String? = null
    @field:NotEmpty(message = "Name is required")
    var name: String? = null
    @field:NotEmpty(message = "E-mail is required")
    var email: String? = null
    @field:NotEmpty(message = "Password is required")
    var password: String? = null
}