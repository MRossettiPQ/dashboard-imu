import { Transform } from 'class-transformer';
import { Dayjs } from 'dayjs';
import { transformDate } from 'src/common/models/models';

export class AccessDto {
  accessToken!: string;
  refreshToken!: string;

  @Transform(transformDate, { toClassOnly: true })
  accessTokenExpiresAt!: Dayjs;

  @Transform(transformDate, { toClassOnly: true })
  refreshTokenExpiresAt!: Dayjs;
}
