import { Transform, Type } from 'class-transformer';
import { BaseModel, BasicResponse, Pagination, transformerDate } from 'src/common/models/models';
import dayjs from 'dayjs';
import { User } from 'src/common/models/user/User';

export class Patient extends BaseModel {
  @Transform(transformerDate)
  birthday?: dayjs.Dayjs | undefined;

  cpf?: string;
  phone?: string;
  cellphone?: string;
  stature?: number;

  @Type(() => User)
  user?: User;
}

class PatientPagination extends Pagination<Patient> {
  @Type(() => Patient)
  declare list: Patient[];
}

export class PatientBasicResponse extends BasicResponse<Patient> {
  @Type(() => Patient)
  declare content: Patient | null;
}

export class PatientPaginationResponse extends BasicResponse<PatientPagination> {
  @Type(() => PatientPagination)
  declare content: PatientPagination | null;
}
