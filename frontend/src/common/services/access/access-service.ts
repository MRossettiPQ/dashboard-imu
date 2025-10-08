import api from 'src/common/services/http-client';
import type { RegisterRequestDto } from 'src/common/models/access/RegisterRequestDto';
import type { LoginRequestDto } from 'src/common/models/access/LoginRequestDto';
import type { AxiosResponse } from 'src/common/models/models';
import { convertResponse, jsonConverter } from 'src/common/models/models';
import type { GenericAbortSignal } from 'axios';
import { UserBasicResponse } from 'src/common/models/user/User';

export const accessService = {
  async login({
    form,
  }: {
    form: LoginRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<UserBasicResponse>> {
    const response = await api.post('/api/access/login', jsonConverter(form));
    return convertResponse(response, UserBasicResponse);
  },
  async register({
    form,
  }: {
    form: RegisterRequestDto;
    signal?: GenericAbortSignal;
  }): Promise<AxiosResponse<UserBasicResponse>> {
    const response = await api.post('/api/access/register', jsonConverter(form));
    return convertResponse(response, UserBasicResponse);
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<UserBasicResponse>> {
    const response = await api.post('/api/access/register', {
      data: {
        refreshToken,
      },
    });
    return convertResponse(response, UserBasicResponse);
  },
  async context(): Promise<AxiosResponse<UserBasicResponse>> {
    const response = await api.get('/api/access/context');
    return convertResponse(response, UserBasicResponse);
  },
};
