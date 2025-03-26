import _ from "lodash";
import { JwtPayload, sign, verify } from "jsonwebtoken";
import { compare } from "bcryptjs";
import settings from "../../settings";
import LogUtil from "../utils/LogUtil";
import { CustomNextFunction, CustomRequest, CustomResponse } from "../utils/ExpressUtil";
import { ApplicationException, HTTPResponseCode } from "../utils/RequestUtil";

export const { resolveToken, createToken, verifyToken, compareCrypt } = new (class AuthorizeJwt {
  async createToken(payload: object): Promise<string> {
    LogUtil.coloring("SERVER:CREATE-TOKEN", "Gerar JWT Token");
    return sign({ ...payload }, settings.secret);
  }

  async resolveToken(token: string): Promise<string | JwtPayload> {
    LogUtil.coloring("SERVER:RESOLVE-TOKEN", "Verificar validade do JWT Token");
    return verify(token, settings.secret, {});
  }

  async compareCrypt(first: string, second: string): Promise<boolean> {
    LogUtil.coloring("SERVER:COMPARE-CRYPT", "Comparar HASH´s");
    return await compare(first, second);
  }

  async verifyToken(req: CustomRequest, _res: CustomResponse, next: CustomNextFunction): Promise<void> {
    LogUtil.coloring("SERVER:VERIFY-TOKEN", "Checar token");
    // Get header token
    const authHeader = req.headers["authorization"];
    let token: string | null = null;

    if (authHeader?.startsWith("Bearer ")) {
      token = authHeader.substring(7, authHeader.length);
    }

    if (_.isUndefined(token) || _.isNil(token)) {
      throw new ApplicationException("Nenhum token foi fornecido", HTTPResponseCode.Forbidden);
    }

    // Check token
    const resultToken = await resolveToken(token);
    if (_.isUndefined(resultToken) || _.isNil(resultToken)) {
      throw new ApplicationException("Token inválido", HTTPResponseCode.Forbidden);
    }

    // Add context object in req
    Object.assign(req, { context: { token } });

    next();
  }
})();
