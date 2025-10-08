import { Transform } from 'class-transformer';
import { Dayjs } from 'dayjs';
import { transformerDateTime } from 'src/common/models/models';

export class AccessDto {
  accessToken!: string;
  refreshToken!: string;

  @Transform(transformerDateTime)
  accessTokenExpiresAt!: Dayjs;

  @Transform(transformerDateTime)
  refreshTokenExpiresAt!: Dayjs;
}
