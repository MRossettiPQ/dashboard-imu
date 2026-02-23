import { Transform } from 'class-transformer';
import { transformerDate } from 'src/api/manual/models';
import {
  type AccessDtoAccessToken,
  AccessDtoAccessTokenExpiresAt,
  type AccessDtoRefreshToken,
  UserDto,
} from 'src/api/generated/models';

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
