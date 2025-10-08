package com.rot.socket.dtos

import com.rot.session.dtos.MeasurementDto
import com.rot.socket.enums.UserSessionType
import java.util.UUID

class SessionContext {
    var id: UUID? = null
    var type: UserSessionType? = null

    // Id da sala == Id do usuario do client
    var room: UUID? = null

    // User id do token de conexão
    var userId: UUID? = null

    // IP do sensor
    var sensorId: String? = null

    var sensors: MutableMap<UUID, Pair<SessionSensorDto, MutableSet<MeasurementDto>>> = mutableMapOf()
}