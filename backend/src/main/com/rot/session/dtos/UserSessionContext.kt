package com.rot.session.dtos

import com.rot.session.enums.SessionContextType
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Contexto em memória de uma sessão ativa do usuário (fisioterapeuta).
 */
class UserSessionContext {
    var userId: UUID? = null // ID DO USUARIO LOGANDO
    var patientId: UUID? = null // ID DO PACIENTE QUE TERÁ OS SENSORES

    /** MAC addresses dos sensores atribuídos a esta sessão */
    var assignedSensors: CopyOnWriteArraySet<String> = CopyOnWriteArraySet()
}

/**
 * Contexto em memória de um sensor conectado ao broker MQTT.
 */
class SensorSessionContext {
    var id: Int? = null
    var ip: String? = null
    var mac: String? = null
    var name: String? = null
    var available: Boolean = true
}


class ClientSessionContext {
    var clientId: String? = null
    var sessionId: UUID? = null

    var type: SessionContextType = SessionContextType.SENSOR
    var user: UserSessionContext? = null
    var sensor: SensorSessionContext? = null

    fun identifier(): String = user?.userId?.toString() ?: sensor?.mac ?: ""
    fun isUser(): Boolean = type == SessionContextType.USER
    fun isSensor(): Boolean = type == SessionContextType.SENSOR

    fun isAvailable(): Boolean = sensor?.available ?: false

    fun setAvailable(available: Boolean) {
        sensor?.available = available
    }
}