import dayjs from 'dayjs';

import type { AxiosResponse } from 'axios';
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

const ISO_DATE_REGEX = /^\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:?\d{2})?)?$/;

function convertDates(data: unknown): unknown {
  if (data === null || data === undefined) return data;

  if (typeof data === 'string' && ISO_DATE_REGEX.test(data)) {
    const parsed = dayjs(data);
    return parsed.isValid() ? parsed : data;
  }

  if (Array.isArray(data)) {
    return data.map(convertDates);
  }

  if (typeof data === 'object') {
    return Object.fromEntries(
      Object.entries(data as Record<string, unknown>).map(([key, value]) => [
        key,
        convertDates(value),
      ]),
    );
  }

  return data;
}

export const customInstance = async <T>(config: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
  const response = await api(config);

  // Se você tem uma função convertDates, aplique-a no data antes de retornar
  if (response.data) {
    response.data = convertDates(response.data);
  }

  return response;
};

export default api;
