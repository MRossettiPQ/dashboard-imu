package com.rot.socket.dtos

import com.rot.socket.enums.MessageType
import java.util.*

open class MessageDto<T> {
    var type: MessageType = MessageType.DEFAULT
    val data: T? = null
}

class RegisterUserDto {
    var uuid: UUID? = null
    var name: String? = null
}

class RegisterSensorDto {
    var ip: String? = null
    var name: String? = null
    var mac: String? = null
}


class SaveSessionDto {
    var uuid: UUID? = null
}

class MessageRegisterUserDto: MessageDto<RegisterUserDto>()
class MessageRegisterSensorDto: MessageDto<RegisterSensorDto>()
class MessageSaveSessionDto: MessageDto<SaveSessionDto>()