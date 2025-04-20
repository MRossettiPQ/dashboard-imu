package com.rot.socket.dtos

import java.util.*

class SessionDto {
    var user: UUID? = null
    var sensors: MutableSet<SensorDto> = mutableSetOf()
}


class SensorDto(
    var mac: String? = null,
    var name: String? = null,
    var ip: String? = null
)