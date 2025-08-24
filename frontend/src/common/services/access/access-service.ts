import api from 'src/common/services/http-client';
import type { RegisterRequestDto } from 'src/common/models/access/RegisterRequestDto';
import type { LoginRequestDto } from 'src/common/models/access/LoginRequestDto';
import type { AxiosResponse } from 'src/common/models/models';
import { BasicResponse, convertResponse, jsonConverter } from 'src/common/models/models';
import type { GenericAbortSignal } from 'axios';
import type { User } from 'src/common/models/user/User';

export const accessService = {
  async login({
    form,
  }: {
    form: LoginRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post('/api/access/login', form);
    return convertResponse(response, BasicResponse<User>);
  },
  async register({
    form,
  }: {
    form: RegisterRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post('/api/access/register', {
      ...jsonConverter(form),
    });
    return convertResponse(response, BasicResponse<User>);
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post('/api/access/register', {
      data: {
        refreshToken,
      },
    });
    return convertResponse(response, BasicResponse<User>);
  },
  async context(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.get('/api/access/context');
    return convertResponse(response, BasicResponse<User>);
  },
};
