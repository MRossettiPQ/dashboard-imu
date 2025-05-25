import api from 'src/common/services/http-client';
import type { AccessDto, BasicResponse, LoginRequestDto, User } from 'src/common/models/models';
import type { AxiosResponse, GenericAbortSignal } from 'axios';

export const accessService = {
  async login({
    form,
  }: {
    form: LoginRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<AccessDto>>> {
    return await api.post<BasicResponse<AccessDto>>('/api/access/login', {
      ...form,
    });
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
