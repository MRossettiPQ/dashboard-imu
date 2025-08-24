import { Type } from 'class-transformer';
import { BaseModel, BasicResponse, Pagination } from 'src/common/models/models';
import { AccessDto } from 'src/common/models/access/AccessDto';

export enum UserRole {
  ADMINISTRATOR = 'ADMINISTRATOR',
  PHYSIOTHERAPIST = 'PHYSIOTHERAPIST',
  PATIENT = 'PATIENT',
}

export class User extends BaseModel {
  username!: string;
  name!: string;
  email!: string;
  password?: string;
  role!: UserRole;

  @Type(() => AccessDto)
  access?: AccessDto;
}

class UserPagination extends Pagination<User> {
  @Type(() => User)
  declare list: User[];
}

export class UserBasicResponse extends BasicResponse<User> {
  @Type(() => User)
  declare content: User | null;
}

export class UserPaginationResponse extends BasicResponse<UserPagination> {
  @Type(() => UserPagination)
  declare content: UserPagination | null;
}
