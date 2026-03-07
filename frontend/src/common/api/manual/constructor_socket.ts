import type {
  AddSensorDto,
  CalibrateSensorDto,
  MeasurementDto,
  SocketClientServerAddSensor,
  SocketClientServerAddSensorOriginIdentifier,
  SocketClientServerCalibrateSensor,
  SocketClientServerCalibrateSensorOriginIdentifier,
  SocketSensorServerSensorMeasurementBlock,
  SocketSensorServerSensorMeasurementBlockOriginIdentifier,
  Uuid,
} from '../generated/models';
import { MessageType, OriginType } from '../generated/models';

export class SensorServerSensorMeasurementBlock
  implements SocketSensorServerSensorMeasurementBlock
{
  constructor(data?: Partial<unknown>) {
    if (data) {
      Object.assign(this, data);
    }
  }

  type: MessageType = MessageType.SERVER_CLIENT_MEASUREMENT;
  date: Date = new Date();
  origin: OriginType = OriginType.BACKEND;
  originIdentifier?: SocketSensorServerSensorMeasurementBlockOriginIdentifier;
  content: MeasurementDto[] = [];
}

export class CalibrateSensorContent implements CalibrateSensorDto {
  sensor!: Uuid;

  constructor(data?: Partial<unknown>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}

export class ClientServerCalibrateSensor implements SocketClientServerCalibrateSensor {
  type: MessageType = MessageType.CLIENT_SERVER_CALIBRATE;
  date: Date = new Date();
  origin: OriginType = OriginType.FRONTEND;
  originIdentifier?: SocketClientServerCalibrateSensorOriginIdentifier;
  content?: CalibrateSensorContent;

  constructor(data?: Partial<unknown>) {
    if (data) {
      Object.assign(this, data);
    }
  }

  setContent(sensor: Uuid) {
    this.content = new CalibrateSensorContent({ sensor });
  }
}

export class AddSensorContent implements AddSensorDto {
  sensor!: Uuid;

  constructor(data?: Partial<unknown>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}

export class ClientServerAddSensor implements SocketClientServerAddSensor {
  type: MessageType = MessageType.CLIENT_SERVER_ADD_SENSOR;
  date: Date = new Date();
  origin: OriginType = OriginType.FRONTEND;
  originIdentifier?: SocketClientServerAddSensorOriginIdentifier;
  content?: AddSensorContent;

  setContent(sensor: Uuid) {
    this.content = new AddSensorContent({ sensor });
  }

  constructor(data?: Partial<unknown>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
