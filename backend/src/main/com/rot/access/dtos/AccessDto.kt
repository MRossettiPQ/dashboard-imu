package com.rot.access.dtos

import java.time.OffsetDateTime

class AccessDto {
    var accessToken: String? = null
    var accessTokenExpiresAt: OffsetDateTime? = null
    var refreshToken: String? = null
    var refreshTokenExpiresAt: OffsetDateTime? = null
}