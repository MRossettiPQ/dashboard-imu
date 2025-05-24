package com.rot.access.dtos

import jakarta.validation.constraints.NotEmpty

class RegisterDto {
    @field:NotEmpty(message = "Username is required")
    lateinit var username: String
    @field:NotEmpty(message = "Name is required")
    lateinit var name: String
    @field:NotEmpty(message = "E-mail is required")
    lateinit var email: String
    @field:NotEmpty(message = "Password is required")
    lateinit var password: String
}