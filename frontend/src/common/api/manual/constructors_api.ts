import type {
  SessionDto,
  SessionDtoId,
  SessionDtoObservation,
  SessionDtoPatient,
  SessionDtoPhysiotherapist,
  SessionDtoSessionDate,
  SessionNodeDto,
  SessionSensorDto,
  SessionSensorDto1,
  SessionSensorDtoClientId,
  SessionSensorDtoIp,
  SessionSensorDtoMac,
  SessionSensorDtoName,
  SessionSensorDtoObservation,
} from '../generated/models';
import { SessionType } from '../generated/models';

export class Session implements SessionDto {
  constructor(data?: Partial<SessionDto>) {
    this.assignValue(data);
  }

  id?: SessionDtoId;
  sessionDate?: SessionDtoSessionDate;
  observation?: SessionDtoObservation;
  patient?: SessionDtoPatient;
  physiotherapist?: SessionDtoPhysiotherapist;
  type: SessionType = SessionType.REAL;

  sessionSensors: SessionSensorDto1[] = [];
  sessionNodes: SessionNodeDto[] = [];

  get getId(): string {
    return String(this.id || '#');
  }

  assignValue(data?: Partial<SessionDto>) {
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
    this.assignValue(data);
  }

  assignValue(data?: Partial<SessionSensorDto>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
