import { Transform } from 'class-transformer';
import { transformerDate } from 'src/common/api/manual/models';
import {
  AccessDtoAccessToken,
  AccessDtoAccessTokenExpiresAt,
  AccessDtoRefreshToken,
  UserDto,
} from '../generated/models';

export class AuthStore {
  loading!: boolean;
  user!: UserDto | null;

  @Transform(transformerDate)
  accessTokenExpiresAt?: AccessDtoAccessTokenExpiresAt | null | undefined;

  accessToken?: AccessDtoAccessToken | null | undefined;

  @Transform(transformerDate)
  refreshTokenExpiresAt?: AccessDtoAccessTokenExpiresAt | null | undefined;

  refreshToken?: AccessDtoRefreshToken | null | undefined;
}
