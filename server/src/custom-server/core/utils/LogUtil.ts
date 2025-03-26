import { Request, Response } from "express";
import dayjs from "dayjs";
import winston, { createLogger, format, transports } from "winston";
import settings from "../../settings";

export enum TextColor {
  Reset = "\x1b[0m",
  Bright = "\x1b[1m",
  Dim = "\x1b[2m",
  Underscore = "\x1b[4m",
  Blink = "\x1b[5m",
  Reverse = "\x1b[7m",
  Hidden = "\x1b[8m",
  Black = "\x1b[30m",
  Red = "\x1b[31m",
  Green = "\x1b[32m",
  Yellow = "\x1b[33m",
  Blue = "\x1b[34m",
  Magenta = "\x1b[35m",
  Cyan = "\x1b[36m",
  White = "\x1b[37m",
  Crimson = "\x1b[38m", // Scarlet
}

export enum BackgroundColor {
  Black = "\x1b[40m",
  Red = "\x1b[41m",
  Green = "\x1b[42m",
  Yellow = "\x1b[43m",
  Blue = "\x1b[44m",
  Magenta = "\x1b[45m",
  Cyan = "\x1b[46m",
  White = "\x1b[47m",
  Crimson = "\x1b[48m",
}

const dateFormat: string = settings.host.logs.dateFormat;

const info: winston.transports.FileTransportInstance = new transports.File({
  filename: "info.log",
  level: "info",
  dirname: settings.cacheDir,
});

const logger: winston.Logger = createLogger({
  format: format.combine(format.errors({ stack: true }), format.json()),
  transports: [info],
});

if (process.env.ENV !== "production") {
  logger.add(
    new transports.Console({
      format: format.simple(),
    }),
  );
}

class Log extends winston.Logger {
  coloringLog(color: TextColor, message: string): string {
    const coloredMessage = `${color}${message}${TextColor.Reset}`;
    logger.info(message);
    // Optei por usar console padrão ao inves do gerado pelo winston para ser mais limpo sem a estrutura json, e poder colorir
    return message;
  }

  logMiddleware(tokens: any, req: Request, res: Response): void {
    const time = dayjs().format("DD/MM/YYYY[-]HH:mm:ss");
    const method = tokens.method(req, res);
    const url = tokens.url(req, res);
    const status = tokens.status(req, res);
    const responseTime = tokens["response-time"](req, res);
    const contentLength = tokens.res(req, res, "content-length");

    const formattedLog = `[CLOG] - ${time} - [MORGAN] - [${method}] - ${url} - ${responseTime} ms - ${status} - ${contentLength}`;
    this.coloringLog(TextColor.Reset, formattedLog);
  }

  coloring(
    local = "C_LOG",
    message: string | string[],
    color: TextColor = TextColor.Reset,
  ): void {
    const formattedDate = dayjs().format(dateFormat);
    let logging = "";
    if (typeof message == "string") {
      logging = message;
    } else {
      logging = `[${message.map((m) => `${m}`).join(", ")}]`;
    }

    this.coloringLog(color, `[CLOG] - ${formattedDate} - [${local}] - ${logging}`);
  }
}

export default new Log();
