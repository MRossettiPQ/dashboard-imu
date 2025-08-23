import type { Component } from 'vue';
import {
  ClassConstructor,
  instanceToPlain,
  plainToInstance,
  Transform,
  Type,
} from 'class-transformer';
import dayjs from 'dayjs';
import type { QBtnProps, QTableColumn } from 'quasar';
import {
  type AxiosResponse as AxiosResponseOriginal,
  AxiosResponseHeaders,
  InternalAxiosRequestConfig,
  RawAxiosResponseHeaders,
} from 'axios';

export type PropsOf<T extends Component> = T extends new (...args: unknown[]) => { $props: infer P }
  ? P
  : never;

export function jsonConverter<T>(instance: T): Record<string, unknown> {
  return instanceToPlain(instance);
}

export function convertResponse<T>(
  response: unknown,
  clazz: ClassConstructor<T>,
): AxiosResponse<T> {
  const responseInstance = plainToInstance(AxiosResponse<T>, response);
  responseInstance.data = plainToInstance(clazz, responseInstance.data);
  return responseInstance;
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

  @Transform(({ value }) => (value ? dayjs(value) : undefined))
  createdAt?: dayjs.Dayjs | undefined;

  @Transform(({ value }) => (value ? dayjs(value) : undefined))
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

export class UserPagination extends Pagination<User> {
  @Type(() => User)
  declare list: User[];
}

export class UserResponse extends BasicResponse<User> {
  @Type(() => User)
  declare content: User | null;
}

export class UserPaginationResponse extends BasicResponse<UserPagination> {
  @Type(() => UserPagination)
  declare content: UserPagination | null;
}

export class PatientPagination extends Pagination<Patient> {
  @Type(() => Patient)
  declare list: Patient[];
}

export class PatientResponse extends BasicResponse<Patient> {
  @Type(() => Patient)
  declare content: Patient | null;
}

export class PatientPaginationResponse extends BasicResponse<PatientPagination> {
  @Type(() => PatientPagination)
  declare content: PatientPagination | null;
}

export class SessionPagination extends Pagination<Session> {
  @Type(() => Session)
  declare list: Session[];
}

export class SessionResponse extends BasicResponse<Session> {
  @Type(() => Session)
  declare content: Session | null;
}

export class SessionPaginationResponse extends BasicResponse<SessionPagination> {
  @Type(() => SessionPagination)
  declare content: SessionPagination | null;
}
