import api from 'src/common/services/http-client';
import type { BasicResponse, LoginRequestDto, RegisterDto, User } from 'src/common/models/models';
import type { AxiosResponse, GenericAbortSignal } from 'axios';

export const accessService = {
  async login({
    form,
  }: {
    form: LoginRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.post<BasicResponse<User>>('/api/access/login', {
      ...form,
    });
  },
  async register({
    form,
  }: {
    form: RegisterDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.post<BasicResponse<User>>('/api/access/register', {
      ...form,
    });
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.post<BasicResponse<User>>('/api/access/register', {
      data: {
        refreshToken,
      },
    });
  },
  async context(): Promise<AxiosResponse<BasicResponse<User>>> {
    return await api.get<BasicResponse<User>>('/api/access/context');
  },
};
