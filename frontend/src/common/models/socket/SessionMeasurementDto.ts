import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';

export class SessionMeasurementDto {
  id?: string;
  sensorName?: string;
  readOrder?: number;

  accelMssX?: number;
  accelMssY?: number;
  accelMssZ?: number;

  accelLinX?: number;
  accelLinY?: number;
  accelLinZ?: number;

  gyroRadsX?: number;
  gyroRadsY?: number;
  gyroRadsZ?: number;

  roll?: number;
  pitch?: number;
  yaw?: number;

  eulerX?: number;
  eulerY?: number;
  eulerZ?: number;

  quaternionX?: number;
  quaternionY?: number;
  quaternionZ?: number;
  quaternionW?: number;

  @Transform(transformerDateTime)
  capturedAt?: dayjs.Dayjs;
}
