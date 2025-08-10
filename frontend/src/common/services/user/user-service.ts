import api from 'src/common/services/http-client';
import { plainToInstance } from 'class-transformer';
import type { BasicResponse, User } from 'src/common/models/models';
import { AxiosResponse } from 'src/common/models/models';

export const userService = {
  async get(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('users');
    return plainToInstance(AxiosResponse<BasicResponse<User>>, response);
  },
};
