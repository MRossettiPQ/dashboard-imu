package com.rot.session.dtos

import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Contexto em memória de uma sessão ativa do usuário (fisioterapeuta).
 */
class UserSessionContext {
    var id: UUID? = null
    var userId: UUID? = null
    var patientId: UUID? = null

    /** MAC addresses dos sensores atribuídos a esta sessão */
    val assignedSensors: CopyOnWriteArraySet<String> = CopyOnWriteArraySet()
}

/**
 * Contexto em memória de um sensor conectado ao broker MQTT.
 */
class SensorSessionContext {
    var mac: String? = null
    var name: String? = null
    var ip: String? = null
    var available: Boolean = true
    var sessionId: UUID? = null

    /** ClientID do Moquette - necessário para rastrear desconexão */
    var clientId: String? = null
}