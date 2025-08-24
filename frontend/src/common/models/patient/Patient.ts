import { Transform, Type } from 'class-transformer';
import { BaseModel, transformDate } from 'src/common/models/models';
import dayjs from 'dayjs';
import { User } from 'src/common/models/user/User';

export class Patient extends BaseModel {
  @Transform(transformDate, { toClassOnly: true })
  birthday?: dayjs.Dayjs | undefined;

  cpf?: string;
  phone?: string;
  stature?: number;

  @Type(() => User)
  user?: User;
}
