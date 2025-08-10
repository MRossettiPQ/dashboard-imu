import type { Component } from 'vue';
import { Transform, Type } from 'class-transformer';
import dayjs from 'dayjs';
import type { QBtnProps, QTableColumn } from 'quasar';
import {
  type AxiosResponse as AxiosResponseOriginal,
  AxiosResponseHeaders,
  InternalAxiosRequestConfig,
  RawAxiosResponseHeaders,
} from 'axios';
import { instanceToPlain } from 'class-transformer';

export type PropsOf<T extends Component> = T extends new (...args: unknown[]) => { $props: infer P }
  ? P
  : never;

export function jsonConverter<T>(instance: T): Record<string, unknown> {
  return instanceToPlain(instance);
}

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

export class AxiosResponse<T, D = unknown> implements AxiosResponseOriginal<T, D> {
  data!: T;
  status!: number;
  statusText!: string;
  headers!: RawAxiosResponseHeaders | AxiosResponseHeaders;
  config!: InternalAxiosRequestConfig<D>;
  request!: unknown;
}

export class LoginRequestDto {
  username!: string;
  password!: string;
}

export class RegisterDto {
  username!: string;
  name!: string;
  email!: string;
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

export class Patient {
  id?: string;

  @Transform(({ value }) => (value ? dayjs(value) : undefined), { toClassOnly: true })
  birthday?: dayjs.Dayjs | undefined;

  cpf?: string;
  phone?: string;
  stature?: number;

  @Type(() => User)
  user?: User;
}

export class Session {
  id?: string;
  type?: SessionType;

  @Transform(({ value }) => (value ? dayjs(value) : undefined), { toClassOnly: true })
  date?: dayjs.Dayjs;
}

export class User {
  id?: string;
  username!: string;
  name!: string;
  email!: string;
  password?: string;
  role!: UserRole;

  @Type(() => AccessDto)
  access?: AccessDto;
}

export class AccessDto {
  accessToken!: string;
  refreshToken!: string;

  @Transform(({ value }) => dayjs(value), { toClassOnly: true })
  accessTokenExpiresAt!: dayjs.Dayjs;

  @Transform(({ value }) => dayjs(value), { toClassOnly: true })
  refreshTokenExpiresAt!: dayjs.Dayjs;
}

export class BasicResponse<T> {
  @Transform(({ value }) => (value ? dayjs(value) : null), { toClassOnly: true })
  date!: dayjs.Dayjs | null;

  code!: number;
  httpCode!: number;
  message!: string | null;

  content!: T | null;
}

export class Pagination<T> {
  list!: T[];
  count!: number;
  pageCount!: number;
  page!: number;
  rpp!: number;
}

export class AuthStore {
  loading!: boolean;
  user!: User | null;

  @Transform(({ value }) => (value ? dayjs(value) : null), { toClassOnly: true })
  accessTokenExpiresAt!: dayjs.Dayjs | null;

  accessToken!: string | null;

  @Transform(({ value }) => (value ? dayjs(value) : null), { toClassOnly: true })
  refreshTokenExpiresAt!: dayjs.Dayjs | null;

  refreshToken!: string | null;
}
