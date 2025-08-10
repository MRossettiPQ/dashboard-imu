import api from 'src/common/services/http-client';
import type { BasicResponse, LoginRequestDto, RegisterDto, User } from 'src/common/models/models';
import { AxiosResponse, jsonConverter } from 'src/common/models/models';
import type { GenericAbortSignal } from 'axios';
import { plainToInstance } from 'class-transformer';

export const accessService = {
  async login({
    form,
  }: {
    form: LoginRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('/api/access/login', {
      ...form,
    });
    return plainToInstance(AxiosResponse<BasicResponse<User>>, response);
  },
  async register({
    form,
  }: {
    form: RegisterDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('/api/access/register', {
      ...jsonConverter(form),
    });
    return plainToInstance(AxiosResponse<BasicResponse<User>>, response);
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('/api/access/register', {
      data: {
        refreshToken,
      },
    });
    return plainToInstance(AxiosResponse<BasicResponse<User>>, response);
  },
  async context(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.get<BasicResponse<User>>('/api/access/context');
    return plainToInstance(AxiosResponse<BasicResponse<User>>, response);
  },
};
