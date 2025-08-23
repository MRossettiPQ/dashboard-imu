import api from 'src/common/services/http-client';
import type { AxiosResponse, BasicResponse } from 'src/common/models/models';
import { UserResponse } from 'src/common/models/models';
import type { User } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';

export const userService = {
  async get(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('users');
    return convertResponse(response, UserResponse);
  },
};
