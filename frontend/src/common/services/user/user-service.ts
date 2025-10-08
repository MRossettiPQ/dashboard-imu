import api from 'src/common/services/http-client';
import type { AxiosResponse } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';
import { UserBasicResponse } from 'src/common/models/user/User';

export const userService = {
  async get(): Promise<AxiosResponse<UserBasicResponse>> {
    const response = await api.post('/api/users');
    return convertResponse(response, UserBasicResponse);
  },
};
