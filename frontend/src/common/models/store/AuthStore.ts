import { Transform } from 'class-transformer';
import { type Dayjs } from 'dayjs';
import { User } from 'src/common/models/user/User';
import { transformerDate } from 'src/common/models/models';

export class AuthStore {
  loading!: boolean;
  user!: User | null;

  @Transform(transformerDate)
  accessTokenExpiresAt!: Dayjs | null;

  accessToken!: string | null;

  @Transform(transformerDate)
  refreshTokenExpiresAt!: Dayjs | null;

  refreshToken!: string | null;
}
