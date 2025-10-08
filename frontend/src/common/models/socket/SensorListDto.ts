import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';
import { Transform } from 'class-transformer';
import { transformerDateTime } from 'src/common/models/models';
import dayjs from 'dayjs';

export class SensorListDto {
  @Transform(transformerDateTime)
  date?: dayjs.Dayjs | undefined;
  sensors: SessionSensorDto[] = [];
}
