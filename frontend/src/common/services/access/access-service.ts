import api from 'src/common/services/http-client';
import type {
  AxiosResponse,
  BasicResponse,
  LoginRequestDto,
  RegisterDto,
  User,
} from 'src/common/models/models';
import { convertResponse, jsonConverter, UserResponse } from 'src/common/models/models';
import type { GenericAbortSignal } from 'axios';

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

    return convertResponse(response, UserResponse);
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
    return convertResponse(response, UserResponse);
  },
  async refreshToken(refreshToken: string): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.post<BasicResponse<User>>('/api/access/register', {
      data: {
        refreshToken,
      },
    });
    return convertResponse(response, UserResponse);
  },
  async context(): Promise<AxiosResponse<BasicResponse<User>>> {
    const response = await api.get<BasicResponse<User>>('/api/access/context');
    return convertResponse(response, UserResponse);
  },
};
