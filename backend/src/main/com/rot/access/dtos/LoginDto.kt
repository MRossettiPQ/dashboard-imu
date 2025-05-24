package com.rot.access.dtos

import jakarta.validation.constraints.NotEmpty

class LoginDto {
    @field:NotEmpty(message = "Username is required")
    lateinit var username: String
    @field:NotEmpty(message = "Password is required")
    lateinit var password: String
}