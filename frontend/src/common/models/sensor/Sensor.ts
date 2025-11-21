import { BaseModel } from 'src/common/models/models';
import { Type } from 'class-transformer';
import { Measurement } from 'src/common/models/measurement/Measurement';

export enum PositionEnum {
  ONE = 'Posição 1',
  TWO = 'Posição 2',
}
export enum SensorType {
  GYROSCOPE = 'GYROSCOPE',
}

export function findPositionEnum(strEn: string | PositionEnum): string | undefined {
  return PositionEnum[strEn as keyof typeof PositionEnum];
}

export const positionOptions = Object.entries(PositionEnum).map(([key, value]) => ({
  label: value,
  value: key,
}));

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
