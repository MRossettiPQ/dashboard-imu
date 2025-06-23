import type { Component } from 'vue';
import type dayjs from 'dayjs';
import type { QBtnProps, QTableColumn } from 'quasar';

export type PropsOf<T extends Component> = T extends new (...args: unknown[]) => { $props: infer P }
  ? P
  : never;

export interface BtnProps<T> extends Omit<Partial<QBtnProps>, 'onClick'> {
  onClick?: (
    evt: Event,
    row?: T,
    go?: (opts?: {
      // eslint-disable-next-line @typescript-eslint/no-redundant-type-constituents
      to?: string | unknown;
      replace?: boolean;
      returnRouterError?: boolean;
    }) => Promise<unknown>,
  ) => void | Promise<void>;
}

export interface TableColumn<T> extends QTableColumn {
  type?: 'button' | 'normal' | undefined;
  props?: BtnProps<T> | undefined;
}

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

export enum SessionType {
  REAL = 'REAL',
  DEMO = 'DEMO',
  GOLD = 'GOLD',
}

export interface Patient {
  id?: string;
  birthday?: dayjs.Dayjs | undefined;
  cpf?: string;
  phone?: string;
  stature?: number;
  user?: User;
}

export interface Session {
  id?: string;
  type?: SessionType;
  date?: dayjs.Dayjs;
}

export interface User {
  id?: string;
  username: string;
  name: string;
  email: string;
  password?: string;
  role: UserRole;
  access?: AccessDto;
}

export interface AccessDto {
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
