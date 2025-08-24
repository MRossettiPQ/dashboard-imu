import api from 'src/common/services/http-client';
import type { AxiosResponse } from 'src/common/models/models';
import { BasicResponse, convertResponse } from 'src/common/models/models';
import type { User } from 'src/common/models/user/User';

export const userService = {
  async get(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post('users');
    return convertResponse(response, BasicResponse<User>);
  },
};
