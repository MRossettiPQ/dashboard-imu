package com.rot.access.dtos

import jakarta.validation.constraints.NotBlank

class RefreshTokenDto {
    @field:NotBlank(message = "O refresh token é obrigatório")
    var refreshToken: String = ""
}