import api from 'src/common/services/http-client';
import type { BasicResponse, User } from 'src/common/models/models';
import type { AxiosResponse } from 'axios';

export const userService = {
  async get(): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.post<BasicResponse<User>>('users');
  },
};
