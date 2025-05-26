import type { Component } from 'vue';
import type dayjs from 'dayjs';

export type PropsOf<T extends Component> = T extends new (...args: unknown[]) => { $props: infer P }
  ? P
  : never;

export interface LoginRequestDto {
  username: string;
  password: string;
}

export interface RegisterDto {
  username: string;
  name: string;
  email: string;
  password?: string;
  passwordConfirm?: string;
}

export enum UserRole {
  ADMINISTRATOR = 'ADMINISTRATOR',
  PHYSIOTHERAPIST = 'PHYSIOTHERAPIST',
  PATIENT = 'PATIENT',
}

export interface Patient {
  id: number;
  user: User;
}

export interface User {
  id: number;
  username: string;
  name: string;
  email: string;
  password?: string;
  role: UserRole;
}

export interface AccessDto extends User {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: dayjs.Dayjs;
  refreshTokenExpiresAt: dayjs.Dayjs;
}

export interface BasicResponse<T> {
  date: dayjs.Dayjs | null;
  code: number;
  message: string | null;
  content: T | null;
}

export interface Pagination<T> {
  list: T[];
  count: number;
  pageCount: number;
  page: number;
  rpp: number;
}

export interface AuthStore {
  loading: boolean;
  user: User | null;
  accessTokenExpiresAt: dayjs.Dayjs | null;
  accessToken: string | null;
  refreshTokenExpiresAt: dayjs.Dayjs | null;
  refreshToken: string | null;
}
