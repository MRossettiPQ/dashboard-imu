package com.rot.session.dtos

import com.rot.socket.dtos.SessionSensorDto
import java.util.*

class UserSessionContext {
    var id: UUID? = null
    var room: UUID? = null

    // User id do token de conexão
    var userId: UUID? = null
    var patientId: UUID? = null

    var sensors: MutableMap<UUID, SessionSensorDto> = mutableMapOf()
}

class SensorSessionContext: SessionSensorDto() {
    var room: UUID? = null
    var available: Boolean = true
}