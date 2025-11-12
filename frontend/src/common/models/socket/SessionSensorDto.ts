import type { PositionEnum, SensorType } from 'src/common/models/sensor/Sensor';

export class SessionSensorDto {
  mac?: string;
  name?: string;
  ip?: string;
  clientId?: string;
  observation?: string;
  position?: PositionEnum;
  type?: SensorType;
}
