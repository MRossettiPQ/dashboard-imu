import { BaseModel } from 'src/common/models/models';
import { Type } from 'class-transformer';
import { Measurement } from 'src/common/models/measurement/Measurement';

export enum PositionEnum {
  ONE = 'ONE',
  TWO = 'TWO',
}
export enum SensorType {
  GYROSCOPE = 'GYROSCOPE',
}

export class Sensor extends BaseModel {
  ip?: string;
  macAddress?: string;
  observation?: string;
  position?: PositionEnum;
  sensorName?: string;
  type?: SensorType;
  @Type(() => Measurement)
  measurements: Measurement[] = [];
}
