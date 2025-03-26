import { CustomNextFunction, CustomRequest, CustomResponse, CustomResult } from "./ExpressUtil";
import Log, { TextColor } from "./LogUtil";

export class ApplicationException extends Error {
  code?: HTTPResponseCode = HTTPResponseCode.BadRequest;

  constructor(message: string, code?: HTTPResponseCode) {
    super(message);
    if (code) {
      this.code = code;
    }
  }
}

export enum HTTPResponseCode {
  Continue = 100,
  SwitchingProtocols = 101,
  OK = 200,
  Created = 201,
  Accepted = 202,
  NonAuthoritativeInformation = 203,
  NoContent = 204,
  ResetContent = 205,
  PartialContent = 206,
  MultipleChoices = 300,
  MovedPermanently = 301,
  Found = 302,
  SeeOther = 303,
  NotModified = 304,
  UseProxy = 305,
  TemporaryRedirect = 307,
  BadRequest = 400,
  Unauthorized = 401,
  PaymentRequired = 402,
  Forbidden = 403,
  NotFound = 404,
  MethodNotAllowed = 405,
  NotAcceptable = 406,
  ProxyAuthenticationRequired = 407,
  RequestTimeout = 408,
  Conflict = 409,
  Gone = 410,
  LengthRequired = 411,
  PreconditionFailed = 412,
  PayloadTooLarge = 413,
  URITooLong = 414,
  UnsupportedMediaType = 415,
  RangeNotSatisfiable = 416,
  ExpectationFailed = 417,
  MisdirectedRequest = 421,
  UnprocessableEntity = 422,
  Locked = 423,
  FailedDependency = 424,
  UpgradeRequired = 426,
  PreconditionRequired = 428,
  TooManyRequests = 429,
  RequestHeaderFieldsTooLarge = 431,
  UnavailableForLegalReasons = 451,
  InternalServerError = 500,
  NotImplemented = 501,
  BadGateway = 502,
  ServiceUnavailable = 503,
  GatewayTimeout = 504,
  HTTPVersionNotSupported = 505,
  VariantAlsoNegotiates = 506,
  InsufficientStorage = 507,
  LoopDetected = 508,
  NotExtended = 510,
  NetworkAuthenticationRequired = 511,
}

export function spaResolver(req: CustomRequest, res: CustomResponse, next: CustomNextFunction): void {
  // Resolver para caso seja servido arquivos estaticos no servidor
  const toServer1: boolean = req.originalUrl.includes("/api");
  const toSwagger: boolean = req.originalUrl.includes("/api-docs");
  const toPublic1: boolean = req.originalUrl === "/";
  if (toServer1 || toSwagger || toPublic1) {
    return next();
  }

  // Redirecionar para telas do aplicativo
  return res.redirect(`/#${req.originalUrl}`);
}

export function middlewareHandler<T>(
  callback:
    | ((req: CustomRequest, res: CustomResponse, next: CustomNextFunction) => Promise<CustomResult<T> | void>)
    | ((req: CustomRequest, res: CustomResponse, next: CustomNextFunction) => Promise<CustomResult<T> | void>)[],
) {
  return asyncHandler(callback, true);
}

export function asyncHandler<T>(
  callback:
    | ((req: CustomRequest, res: CustomResponse, next: CustomNextFunction) => Promise<CustomResult<T> | void>)
    | ((req: CustomRequest, res: CustomResponse, next: CustomNextFunction) => Promise<CustomResult<T> | void>)[],
  middleware = false,
) {
  return async (req: CustomRequest, res: CustomResponse, next: CustomNextFunction): Promise<void> => {
    // Normalização das chamadas para um array
    let jobs: ((req: CustomRequest, res: CustomResponse, next: CustomNextFunction) => Promise<CustomResult<T> | void>)[] = [];

    if (!middleware) {
      Log.coloring("SERVER:REQUEST", `[${req.method}] - ${req.originalUrl}`, TextColor.Magenta);
    }

    if (Array.isArray(callback)) {
      jobs = callback;
    } else if (typeof callback === "function") {
      jobs = [callback];
    }

    // Chamada das funções de middleware e do controller
    try {
      for (const job of jobs) {
        const result: CustomResult<T> | void = await job(req, res, next);
        if (result) {
          res.status(result.code).send(result.response);
          break;
        } else {
          // Não é pra cair nesse caso
          new ApplicationException("Não é um middleware e nem um result válido");
        }
      }
    } catch (e) {
      Log.coloring("REQUEST_UTIL", e.toString());
      if (e instanceof ApplicationException) {
        Log.coloring("REQUEST_UTIL", e?.message);
        res.status(e.code || 500).send({ message: `${e.message}` });
        return;
      } else if (e instanceof Error) {
        // Deverá cair nesse caso apenas se for um erro não tratado ('internal error')
        Log.coloring("REQUEST_UTIL", e?.message);
        res.status(500).send({ message: `${e.message}` });
        return;
      }
    }
  };
}
