import { Transform, Type } from 'class-transformer';
import { Dayjs } from 'dayjs';
import { BaseModel, BasicResponse, Pagination, transformerDate } from 'src/common/models/models';

export enum SessionType {
  REAL = 'REAL',
  DEMO = 'DEMO',
  GOLD = 'GOLD',
}

export class Session extends BaseModel {
  type?: SessionType;

  @Transform(transformerDate, { toClassOnly: true })
  date?: Dayjs;
}

class SessionPagination extends Pagination<Session> {
  @Type(() => Session)
  declare list: Session[];
}

export class SessionBasicResponse extends BasicResponse<Session> {
  @Type(() => Session)
  declare content: Session | null;
}

export class SessionPaginationResponse extends BasicResponse<SessionPagination> {
  @Type(() => SessionPagination)
  declare content: SessionPagination | null;
}
