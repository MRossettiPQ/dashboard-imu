import {
  ClassConstructor,
  instanceToPlain,
  plainToClassFromExist,
  plainToInstance,
  Transform,
  TransformFnParams,
} from 'class-transformer';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import type { QBtnProps, QTableColumn } from 'quasar';
import {
  type AxiosResponse as AxiosResponseOriginal,
  AxiosResponseHeaders,
  InternalAxiosRequestConfig,
  RawAxiosResponseHeaders,
} from 'axios';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

export function jsonConverter<T>(instance: T): Record<string, unknown> {
  return instanceToPlain(instance);
}

export function convertResponse<T>(
  response: unknown,
  clazz: ClassConstructor<T>,
): AxiosResponse<T> {
  const responseInstance = plainToInstance(AxiosResponse<T>, response);
  const data = instanceToPlain(responseInstance.data);
  responseInstance.data = plainToClassFromExist<T, unknown>(new clazz(), data);
  return responseInstance;
}

export interface BtnProps<T> extends Omit<Partial<QBtnProps>, 'onClick'> {
  onClick?: (
    evt: Event,
    row?: T,
    go?: (opts?: {
      to?: string;
      replace?: boolean;
      returnRouterError?: boolean;
    }) => Promise<unknown>,
  ) => void | Promise<void>;
}

export interface TableColumn<T> extends QTableColumn {
  type?: 'button' | 'normal' | undefined;
  props?: BtnProps<T> | undefined;
}

const LOCAL_TZ = dayjs.tz.guess();

export function transformerDateTime({ value, type }: TransformFnParams) {
  const typeNum = Number(type);

  if (typeNum === 0) {
    // toClassOnly
    if (!value) return undefined;
    return dayjs(value).isValid() ? dayjs.tz(value, LOCAL_TZ) : undefined;
  }

  if (typeNum === 1) {
    // toPlainOnly
    if (!value) return null;
    return dayjs(value).tz(LOCAL_TZ).format('YYYY-MM-DD HH:mm:ssZ');
  }

  return value;
}
export function transformerDate({ value, type }: TransformFnParams) {
  const typeNum = Number(type);

  if (typeNum === 0) {
    // toClassOnly
    if (!value) return undefined;
    return dayjs(value).isValid() ? dayjs(value).tz(LOCAL_TZ).startOf('day') : undefined;
  }

  if (typeNum === 1) {
    // toPlainOnly
    if (!value) return null;
    return dayjs(value).tz(LOCAL_TZ).format('YYYY-MM-DD');
  }

  return value;
}

export class BaseModel {
  id?: string = undefined;

  @Transform(transformerDateTime)
  createdAt?: Dayjs | undefined = undefined;

  @Transform(transformerDateTime)
  updatedAt?: Dayjs | undefined = undefined;
}

export class AxiosResponse<T, D = unknown> implements AxiosResponseOriginal<T, D> {
  status!: number;
  statusText!: string;
  headers!: RawAxiosResponseHeaders | AxiosResponseHeaders;
  config!: InternalAxiosRequestConfig<D>;
  request!: unknown;
  data!: T;
}

export class BasicResponse<T> {
  @Transform(transformerDateTime)
  date!: Dayjs | null;

  code!: number;
  httpCode!: number;
  message!: string | null;
  content!: T | null;
}

export class Pagination<T> {
  count!: number;
  pageCount!: number;
  page!: number;
  rpp!: number;
  list!: T[];
}
