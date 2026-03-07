import type {
  ArticulationDto,
  ArticulationDtoDescription,
  ArticulationDtoId,
  ArticulationDtoType,
  MeasurementDto,
  MovementDto,
  MovementDtoId,
  MovementDtoMovementType,
  MovementDtoObservation,
  MovementDtoType,
  SensorDto,
  SensorDtoBodySegment,
  SensorDtoId,
  SensorDtoIp,
  SensorDtoObservation,
  SensorDtoSensorInfo,
  SessionDto,
  SessionDtoId,
  SessionDtoObservation,
  SessionDtoPatient,
  SessionDtoPhysiotherapist,
  SessionDtoSessionDate,
  SessionSensorDto,
  SessionSensorDtoClientId,
  SessionSensorDtoIp,
  SessionSensorDtoMac,
  SessionSensorDtoName,
  SessionSensorDtoObservation,
} from '../generated/models';
import { SessionType } from '../generated/models';

export class Movement implements MovementDto {
  constructor(data?: Partial<MovementDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }

  id?: MovementDtoId;
  type?: MovementDtoType;
  movementType?: MovementDtoMovementType;
  observation?: MovementDtoObservation;
  sensors: SensorDto[] = [];

  get hasSensors(): boolean {
    return this.sensors.length > 0;
  }
}

export class Session implements SessionDto {
  constructor(data?: Partial<MovementDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }
  id?: SessionDtoId;
  sessionDate?: SessionDtoSessionDate;
  observation?: SessionDtoObservation;
  patient?: SessionDtoPatient;
  physiotherapist?: SessionDtoPhysiotherapist;
  type: SessionType = SessionType.REAL;
  articulations: ArticulationDto[] = [];
}

export class Articulation implements ArticulationDto {
  id?: ArticulationDtoId;
  type?: ArticulationDtoType;
  description?: ArticulationDtoDescription;
  movements: Movement[] = [];

  constructor(data?: Partial<ArticulationDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}

export class SessionSensor implements SessionSensorDto {
  clientId?: SessionSensorDtoClientId;
  ip?: SessionSensorDtoIp;
  name?: SessionSensorDtoName;
  mac?: SessionSensorDtoMac;
  observation?: SessionSensorDtoObservation;

  constructor(data?: Partial<SessionSensorDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}

export class Sensor implements SensorDto {
  id?: SensorDtoId;
  ip?: SensorDtoIp;
  sensorInfo?: SensorDtoSensorInfo;
  bodySegment?: SensorDtoBodySegment;
  observation?: SensorDtoObservation;
  measurements: MeasurementDto[] = [];

  constructor(data?: Partial<SensorDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
