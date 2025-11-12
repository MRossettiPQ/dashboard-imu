package com.rot.socket.dtos

import com.rot.session.dtos.MeasurementDto
import com.rot.session.enums.MovementEnum
import com.rot.session.enums.PositionEnum
import com.rot.session.enums.ProcedureEnum
import com.rot.session.enums.SensorType
import com.rot.socket.enums.MessageType
import com.rot.socket.enums.OriginType
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

open class MessageDto<T>(var type: MessageType = MessageType.DEFAULT, var content: T? = null) {
    val date: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    val origin: OriginType = OriginType.UNKNOWN
}

// Client -> Server
class AddSensorDto {
    lateinit var sensor: UUID
}

class RemoveSensorDto {
    lateinit var sensor: UUID
}

class CalibrateSensorDto {
    lateinit var sensor: UUID
}

class SaveSessionDto {
    var patientId: UUID? = null
    var observation: String? = null

    var procedure: ProcedureEnum = ProcedureEnum.SIMPLE

    var movementType: MovementEnum = MovementEnum.SIMPLE
    var movementObservation: String? = null
}


// Server -> Sensor
class StartCommandDto {
    val command: MessageType = MessageType.SERVER_SENSOR_START
}

class StopCommandDto {
    val command: MessageType = MessageType.SERVER_SENSOR_STOP
}

class CalibrateCommandDto {
    val command: MessageType = MessageType.SERVER_SENSOR_CALIBRATE
}

class JoinedRoomDto {
    val message: MessageType = MessageType.SERVER_SENSOR_JOINED_ROOM
}

class RemovedRoomDto {
    val message: MessageType = MessageType.SERVER_SENSOR_REMOVED_ROOM
}

class SessionSensorDto {
    var clientId: UUID? = null
    var ip: String? = null
    var name: String? = null
    var mac: String? = null
    var observation: String? = null
    var position: PositionEnum? = null
    var type: SensorType = SensorType.GYROSCOPE

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SessionSensorDto) return false
        return ip == other.ip
    }

    override fun hashCode(): Int {
        return ip?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[ip: $ip]"
    }
}

class MessageSensorMeasurementBlock : MessageDto<Set<MeasurementDto>>(type = MessageType.SENSOR_SERVER_MEASUREMENT)
class MessageClientMeasurementBlock : MessageDto<Set<MeasurementDto>>(type = MessageType.SERVER_CLIENT_MEASUREMENT)


// Sensor -> Server
class MessageSessionSensorDto : MessageDto<SessionSensorDto>(type = MessageType.SENSOR_SERVER_REGISTER_SENSOR)


// Server -> Client
class MessageSensorListDto : MessageDto<MutableList<SessionSensorDto>>(type = MessageType.SERVER_CLIENT_SENSOR_LIST)


// Server -> Sensor
class MessageStartCommandDto : MessageDto<StartCommandDto>(type = MessageType.SERVER_SENSOR_START)
class MessageStopCommandDto : MessageDto<StopCommandDto>(type = MessageType.SERVER_SENSOR_STOP)
class MessageCalibrateCommandDto : MessageDto<CalibrateCommandDto>(type = MessageType.SERVER_SENSOR_CALIBRATE)
class MessageJoinedRoomDto : MessageDto<JoinedRoomDto>(type = MessageType.SERVER_SENSOR_JOINED_ROOM)
class MessageRemovedRoomDto : MessageDto<RemovedRoomDto>(type = MessageType.SERVER_SENSOR_REMOVED_ROOM)

// Client -> Server
class MessageAddSensorDto : MessageDto<AddSensorDto>(type = MessageType.CLIENT_SERVER_ADD_SENSOR)
class MessageRemoveSensorDto : MessageDto<RemoveSensorDto>(type = MessageType.CLIENT_SERVER_REMOVE_SENSOR)
class MessageCalibrateSensorDto : MessageDto<CalibrateSensorDto>(type = MessageType.CLIENT_SERVER_REMOVE_SENSOR)
class MessageSaveSessionDto : MessageDto<SaveSessionDto>(type = MessageType.CLIENT_SERVER_SAVE_SESSION)