import { NextFunction, Request, Response, Express } from "express";
import User from "../../app/user/models/User";
import dayjs from "dayjs";

interface CustomResult<T> {
  // Log
  local: string;
  color?: string;
  log?: string;

  // JSON, TEXT PLAIN ...
  type?: string;
  code?: number;

  // Response
  response: {
    date: dayjs.Dayjs;
    message?: string | null;
    content?: T | T[] | { [key: string]: unknown } | { [key: string]: unknown }[] | null;
  };
}

interface ContextRequest {
  token?: string;
  user?: User;
}

interface CustomRequest extends Request {
  context?: ContextRequest;
}

type CustomNextFunction = NextFunction;

type CustomResponse = Response;

type CustomExpress = Express;

export type { CustomResponse, CustomNextFunction, CustomExpress, CustomRequest, CustomResult, ContextRequest };
