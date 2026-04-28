import type { TransformFnParams } from 'class-transformer';
import dayjs from 'dayjs';
import type { QBtnProps, QTableColumn } from 'quasar';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

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

export class Pagination<T> {
  count!: number;
  hasMore!: boolean;
  page!: number;
  rpp!: number;
  list!: T[];
  extra?: unknown;
}
