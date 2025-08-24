import { Transform } from 'class-transformer';
import { type Dayjs } from 'dayjs';
import { User } from 'src/common/models/user/User';
import { transformDate } from 'src/common/models/models';

export class AuthStore {
  loading!: boolean;
  user!: User | null;

  @Transform(transformDate, { toClassOnly: true })
  accessTokenExpiresAt!: Dayjs | null;

  accessToken!: string | null;

  @Transform(transformDate, { toClassOnly: true })
  refreshTokenExpiresAt!: Dayjs | null;

  refreshToken!: string | null;
}
