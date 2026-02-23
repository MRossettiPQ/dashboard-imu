package com.rot.socket.dtos

import com.rot.gonimetry.enums.ArticulationEnum
import com.rot.gonimetry.enums.MovementEnum
import com.rot.measurement.dtos.MeasurementDto
import com.rot.socket.enums.MessageType
import com.rot.socket.enums.OriginType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

open class MessageDto<T>(var type: MessageType = MessageType.DEFAULT) {
    var date: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var origin: OriginType = OriginType.UNKNOWN
    var originIdentifier: String? = null
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

    var procedure: ArticulationEnum = ArticulationEnum.SAMPLE

    var movementType: MovementEnum = MovementEnum.SAMPLE
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



// Sensor -> Server
@Schema(name = "SocketSensorServerSessionSensor")
class MessageSensorServerSessionSensorDto : MessageDto<SessionSensorDto>(type = MessageType.SENSOR_SERVER_REGISTER_SENSOR) {
    var content: SessionSensorDto? = null
}

@Schema(name = "SocketSensorServerSensorMeasurementBlock")
class MessageSensorServerMeasurementBlock : MessageDto<MutableList<MeasurementDto>>(type = MessageType.SENSOR_SERVER_MEASUREMENT) {
    var content: MutableList<MeasurementDto>? = mutableListOf()
}


// Server -> Client
@Schema(name = "SocketServerClientSensorList")
class MessageServerClientSensorListDto : MessageDto<MutableList<SessionSensorDto>>(type = MessageType.SERVER_CLIENT_SENSOR_LIST) {
    var content: MutableList<SessionSensorDto>? = mutableListOf()
}

@Schema(name = "SocketServerClientMeasurementBlock")
class MessageServerClientMeasurementBlock : MessageDto<MutableList<MeasurementDto>>(type = MessageType.SERVER_CLIENT_MEASUREMENT) {
    var content: MutableList<MeasurementDto>? = mutableListOf()
}


// Server -> Sensor
@Schema(name = "SocketServerSensorStartCommand")
class MessageServerSensorStartCommandDto : MessageDto<StartCommandDto>(type = MessageType.SERVER_SENSOR_START) {
    var content: SessionSensorDto? = null
}

@Schema(name = "SocketServerSensorStopCommand")
class MessageServerSensorStopCommandDto : MessageDto<StopCommandDto>(type = MessageType.SERVER_SENSOR_STOP) {
    var content: StopCommandDto? = null
}

@Schema(name = "SocketServerSensorCalibrateSensor")
class MessageCalibrateCommandDto : MessageDto<CalibrateCommandDto>(type = MessageType.SERVER_SENSOR_CALIBRATE) {
    var content: CalibrateCommandDto? = null
}

@Schema(name = "SocketServerSensorJoinedRoom")
class MessageServerSensorJoinedRoomDto : MessageDto<JoinedRoomDto>(type = MessageType.SERVER_SENSOR_JOINED_ROOM) {
    var content: JoinedRoomDto? = null
}

@Schema(name = "SocketServerSensorRemovedRoom")
class MessageServerSensorRemovedRoomDto : MessageDto<RemovedRoomDto>(type = MessageType.SERVER_SENSOR_REMOVED_ROOM) {
    var content: RemovedRoomDto? = null
}

// Client -> Server
@Schema(name = "SocketClientServerAddSensor")
class MessageClientServerAddSensorDto : MessageDto<AddSensorDto>(type = MessageType.CLIENT_SERVER_ADD_SENSOR) {
    var content: AddSensorDto? = null
}

@Schema(name = "SocketClientServerRemoveSensor")
class MessageClientServerRemoveSensorDto : MessageDto<RemoveSensorDto>(type = MessageType.CLIENT_SERVER_REMOVE_SENSOR) {
    var content: RemoveSensorDto? = null
}

@Schema(name = "SocketClientServerCalibrateSensor")
class MessageClientServerCalibrateSensorDto : MessageDto<CalibrateSensorDto>(type = MessageType.CLIENT_SERVER_REMOVE_SENSOR) {
    var content: CalibrateSensorDto? = null
}

@Schema(name = "SocketClientServerSaveSession")
class MessageClientServerSaveSessionDto : MessageDto<SaveSessionDto>(type = MessageType.CLIENT_SERVER_SAVE_SESSION) {
    var content: SaveSessionDto? = null
}