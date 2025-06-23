package com.rot.access.dtos

import java.time.LocalDateTime

class AccessDto {
    var accessToken: String? = null
    var accessTokenExpiresAt: LocalDateTime? = null
    var refreshToken: String? = null
    var refreshTokenExpiresAt: LocalDateTime? = null
}