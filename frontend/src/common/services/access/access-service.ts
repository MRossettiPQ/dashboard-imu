import api from 'src/common/services/http-client';
import type { AccessDto, BasicResponse, User } from 'src/common/models/models';
import type { AxiosResponse } from 'axios';

export const accessService = {
  async login(): Promise<AxiosResponse<BasicResponse<AccessDto>>> {
    return await api.post<BasicResponse<AccessDto>>('/api/access/login');
  },
  async register(): Promise<AxiosResponse<BasicResponse<AccessDto>>> {
    return await api.post<BasicResponse<AccessDto>>('/api/access/register');
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<BasicResponse<AccessDto>>> {
    return await api.post<BasicResponse<AccessDto>>('/api/access/register', {
      data: {
        refreshToken,
      },
    });
  },
  async context(): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.get<BasicResponse<AccessDto>>('/api/access/context');
  },
};
