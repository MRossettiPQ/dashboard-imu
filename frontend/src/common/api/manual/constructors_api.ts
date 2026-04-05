import type {
  MeasurementRead,
  NodeSensorCreateOrUpdate,
  SessionCreateOrUpdate,
  SessionCreateOrUpdateId,
  SessionCreateOrUpdateObservation,
  SessionNodeCreateOrUpdate,
  SessionRead,
  SessionSensorCreateOrUpdate,
  SessionSensorCreateOrUpdateId,
  SessionSensorCreateOrUpdateIp,
  SessionSensorCreateOrUpdateObservation,
  SessionSensorCreateOrUpdateSensorInfoId,
  SessionSensorCreateOrUpdateSessionId,
  SessionSensorRead,
  SessionType,
  Uuid,
} from 'src/common/api/generated/models';

export class SessionSensor implements SessionSensorCreateOrUpdate {
  id?: SessionSensorCreateOrUpdateId;
  ip?: SessionSensorCreateOrUpdateIp;
  observation?: SessionSensorCreateOrUpdateObservation;
  sessionId?: SessionSensorCreateOrUpdateSessionId;
  sensorInfoId?: SessionSensorCreateOrUpdateSensorInfoId;
  nodeSensors: NodeSensorCreateOrUpdate[] = [];

  measurements: Set<MeasurementRead> = new Set();

  constructor(data?: Partial<SessionSensorRead>) {
    this.assignValue(data);
  }

  get measurementsArray(): MeasurementRead[] {
    return Array.from(this.measurements);
  }

  assignValue(data?: Partial<SessionSensorRead>) {
    if (data) {
      Object.assign(this, data);
      this.sensorInfoId = data.sensorInfo?.id ?? null;
      this.sessionId = data.sessionId ?? null;
    }
  }
}

export class SessionState implements SessionCreateOrUpdate {
  id?: SessionCreateOrUpdateId;
  patientId?: Uuid;
  type?: SessionType;
  observation?: SessionCreateOrUpdateObservation;

  get getId(): string {
    return String(this.id || '#');
  }

  private _sessionSensors: Set<SessionSensor> = new Set();
  private _sessionNodes: Set<SessionNodeCreateOrUpdate> = new Set();

  get sessionSensors(): SessionSensor[] {
    return Array.from(this._sessionSensors);
  }

  set sessionSensors(values: SessionSensor[] | undefined) {
    this._sessionSensors = values ? new Set(values) : new Set();
  }

  get sessionNodes(): SessionNodeCreateOrUpdate[] {
    return Array.from(this._sessionNodes);
  }

  set sessionNodes(values: SessionNodeCreateOrUpdate[] | undefined) {
    this._sessionNodes = values ? new Set(values) : new Set();
  }

  constructor(data?: Partial<SessionRead>) {
    this.assignValue(data);
  }

  assignValue(data?: Partial<SessionRead>) {
    if (data) {
      Object.assign(this, data);
      if (data.patient?.id) this.patientId = data.patient.id;
    }
  }

  addSensor(sensor: SessionSensor) {
    this._sessionSensors.add(sensor);
  }

  removeSensor(sensor: SessionSensor) {
    this._sessionSensors.delete(sensor);
  }
}
