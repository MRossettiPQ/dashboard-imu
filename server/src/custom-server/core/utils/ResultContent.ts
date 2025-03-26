import { CustomResult } from "./ExpressUtil";
import { HTTPResponseCode } from "./RequestUtil";
import dayjs from "dayjs";

interface IResultClass<T> extends CustomResult<T> {
  withContent(content: T | T[]): IResultClass<T>;

  withMessage(message: string): IResultClass<T>;

  withCode(code: HTTPResponseCode): IResultClass<T>;

  ok(): CustomResult<T>;

  badRequest(): CustomResult<T>;

  unauthorized(): CustomResult<T>;

  notFound(): CustomResult<T>;

  build(): CustomResult<T>;
}

export default class Result<T> implements IResultClass<T> {
  withContent(content: T | T[]): IResultClass<T> {
    this.response.content = content;
    return this;
  }

  withMessage(message: string): IResultClass<T> {
    this.response.message = message;
    return this;
  }

  withCode(code: HTTPResponseCode): IResultClass<T> {
    this.code = code.valueOf();
    return this;
  }

  badRequest(): CustomResult<T> {
    this.code = HTTPResponseCode.BadRequest;
    return this;
  }

  notFound(): CustomResult<T> {
    this.code = HTTPResponseCode.NotFound;
    return this;
  }

  unauthorized(): CustomResult<T> {
    this.code = HTTPResponseCode.Unauthorized;
    return this;
  }

  ok(): CustomResult<T> {
    this.code = HTTPResponseCode.OK;
    return this;
  }

  build(): CustomResult<T> {
    return this;
  }

  /*
    Caso não alterar será um code 200
  */
  code: number = HTTPResponseCode.OK;
  local: string;

  response: {
    date: dayjs.Dayjs;
    message?: string | null;
    content?: T | T[] | { [key: string]: unknown } | { [key: string]: unknown }[] | null;
  } = {
    date: dayjs(),
  };
}

export class Pagination<T> implements IResultClass<T> {
  withPagination(list?: T[], count?: number | string, page?: number | string, rpp?: number | string): IResultClass<T> {
    this.response.content = {
      list: list || [],
      count: Number(count || 0),
      page: Number(page || 0),
      pageCount: Number(count || 0) / Number(rpp || 0),
      rpp: Number(rpp || 0),
    };
    return this;
  }

  withContent(): IResultClass<T> {
    return this;
  }

  withMessage(message: string): IResultClass<T> {
    this.response.message = message;
    return this;
  }

  withCode(code: HTTPResponseCode): IResultClass<T> {
    this.code = code.valueOf();
    return this;
  }

  badRequest(): CustomResult<T> {
    this.code = HTTPResponseCode.BadRequest;
    return this;
  }

  notFound(): CustomResult<T> {
    this.code = HTTPResponseCode.NotFound;
    return this;
  }

  unauthorized(): CustomResult<T> {
    this.code = HTTPResponseCode.Unauthorized;
    return this;
  }

  ok(): CustomResult<T> {
    this.code = HTTPResponseCode.OK;
    return this;
  }

  build(): CustomResult<T> {
    return this;
  }

  /*
    Caso não alterar será um code 200
  */
  code: number = HTTPResponseCode.OK;
  local: string;

  response: {
    date: dayjs.Dayjs;
    message?: string | null;
    content?: {
      list: T[] | { [key: string]: unknown }[];
      count: number;
      pageCount: number;
      page: number;
      rpp: number;
    };
  } = {
    date: dayjs(),
  };
}
