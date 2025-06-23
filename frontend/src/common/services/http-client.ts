import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { Cookies } from 'quasar';
import { notify } from 'src/common/utils/NotifyUtils';
import { CookieType } from 'src/common/utils/cookieUtils';

const api = axios.create({
  baseURL: process.env.SERVER_API,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function checkAxiosStatus(e: unknown, code: number): boolean {
  return e instanceof AxiosError && e?.response?.status === code;
}

export interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
}

api.interceptors.request.use(
  (config) => {
    if ('skipAuth' in config && config.skipAuth) {
      return config;
    }

    const token = Cookies.get(CookieType.ACCESS_TOKEN);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    if (error instanceof Error) {
      return Promise.reject(error);
    }
    return Promise.reject(new Error(String(error)));
  },
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    let message = 'An unexpected error occurred. Please try again.';

    if (axios.isAxiosError(error)) {
      if (error.response?.data?.message) {
        message = error.response?.data?.message;
      }

      switch (error.response?.status) {
        case 403:
          message = message ?? 'Access denied. You do not have permission';
          break;
        case 401:
          message = message ?? 'User not authorized';
          break;
        case 400:
          if (error.response?.data?.violations[0].message) {
            message = error.response?.data?.violations[0].message;
          }
          break;
        default:
          console.error('Check axios error handler:', error.response ?? error.message);
          break;
      }
    }

    notify.error(message);
    if (error instanceof Error) {
      return Promise.reject(error);
    }
    return Promise.reject(new Error(String(error)));
  },
);

export default api;
