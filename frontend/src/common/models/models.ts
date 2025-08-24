import type { Component } from 'vue';
import {
  ClassConstructor,
  instanceToPlain,
  plainToInstance,
  Transform,
  Type,
  TypeHelpOptions,
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

  console.log('RESPONSE INSTANCE', responseInstance);
  responseInstance.data = plainToInstance(clazz, responseInstance.data);
  console.log('DATA INSTANCE', responseInstance.data);
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

export function transformDate({ value }: { value: string }) {
  return value ? dayjs(value) : undefined;
}

type Constructor<T> = new (...args: unknown[]) => T;

export class BaseModel {
  id?: string;

  @Transform(transformDate, { toClassOnly: true })
  createdAt?: Dayjs | undefined;

  @Transform(transformDate, { toClassOnly: true })
  updatedAt?: Dayjs | undefined;
}

export class AxiosResponse<T, D = unknown> implements AxiosResponseOriginal<T, D> {
  @Type((options?: TypeHelpOptions) => {
    const ctor = (options?.newObject as AxiosResponse<T, D>).type ?? Object;
    console.log('CTOR AXIOS: ', ctor);
    return ctor;
  })
  data!: T;
  status!: number;
  statusText!: string;
  headers!: RawAxiosResponseHeaders | AxiosResponseHeaders;
  config!: InternalAxiosRequestConfig<D>;
  request!: unknown;

  private readonly type?: Constructor<T>;

  constructor(type?: Constructor<T>) {
    if (type) this.type = type;
  }
}

export class BasicResponse<T> {
  @Transform(transformDate, { toClassOnly: true })
  date!: Dayjs | null;

  code!: number;
  httpCode!: number;
  message!: string | null;

  @Type((options?: TypeHelpOptions) => {
    const ctor = (options?.newObject as BasicResponse<T>).type ?? Object;
    console.log('CTOR BASIC: ', ctor);
    return ctor;
  })
  content!: T | null;

  private readonly type?: Constructor<T>;

  constructor(type?: Constructor<T>) {
    if (type) this.type = type;
  }
}

export class Pagination<T> {
  @Type((options?: TypeHelpOptions) => {
    const ctor = (options?.newObject as Pagination<T>).type ?? Object;
    console.log('CTOR PAGINATION: ', ctor);
    return ctor;
  })
  list!: T[];

  count!: number;
  pageCount!: number;
  page!: number;
  rpp!: number;

  private readonly type?: Constructor<T>;

  constructor(type?: Constructor<T>) {
    if (type) this.type = type;
  }
}
