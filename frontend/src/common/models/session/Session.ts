import { Transform } from 'class-transformer';
import { Dayjs } from 'dayjs';
import { BaseModel, transformDate } from 'src/common/models/models';

export enum SessionType {
  REAL = 'REAL',
  DEMO = 'DEMO',
  GOLD = 'GOLD',
}

export class Session extends BaseModel {
  type?: SessionType;

  @Transform(transformDate, { toClassOnly: true })
  date?: Dayjs;
}
