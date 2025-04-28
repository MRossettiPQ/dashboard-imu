import dayjs from 'dayjs';

export default class DateUtils {
  static ISO_DATETIME_FORMAT = 'YYYY-MM-DD[T]HH:mm:ss';
  static ISO_DATE_FORMAT = 'YYYY-MM-DD';
  static ISO_TIME_FORMAT = 'HH:mm:ss';

  static DATETIME_FORMAT = 'DD/MM/YYYY HH:mm';
  static DATETIME_SECONDS_FORMAT = 'DD/MM/YYYY HH:mm:ss';
  static DATE_FORMAT = 'DD/MM/YYYY';
  static TIME_FORMAT = 'HH:mm';

  static nowIso(): string {
    return DateUtils.getDateTimeISOFormatted();
  }

  static now(): string {
    return DateUtils.getDateTimeFormatted();
  }

  static getDateTimeISOFormatted(dateTime: string | null = null): string {
    return dayjs(dateTime).format(DateUtils.ISO_DATETIME_FORMAT);
  }

  static getDateISOFormatted(date: string): string {
    return dayjs(date).format(DateUtils.ISO_DATE_FORMAT);
  }

  static getTimeISOFormatted(time: string): string {
    return dayjs(time).format(DateUtils.ISO_TIME_FORMAT);
  }

  static getDateTimeFormatted(dateTime: string | null = null): string {
    return dayjs(dateTime).format(DateUtils.DATETIME_FORMAT);
  }

  static getDateTimeSecondsFormatted(dateTime: string): string {
    return dayjs(dateTime).format(DateUtils.DATETIME_SECONDS_FORMAT);
  }

  static getDateFormatted(date: string): string {
    return dayjs(date).format(DateUtils.DATE_FORMAT);
  }

  static getTimeFormatted(time: string): string {
    return dayjs(time).format(DateUtils.TIME_FORMAT);
  }
}
