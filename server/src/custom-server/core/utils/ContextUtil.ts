import LogUtil from "./LogUtil";
import _ from "lodash";
import { ContextRequest, CustomRequest } from "./ExpressUtil";
import { resolveToken } from "../middleware/AuthorizeJwt";
import { ApplicationException, HTTPResponseCode } from "./RequestUtil";
import User from "../../app/user/models/User";

export const { getUserContextId, getContext, getUserContext } = new (class ContextUtil {
  async getUserContextId(req: CustomRequest): Promise<number> {
    LogUtil.coloring("SERVER:CONTEXT", "getUserContextId");
    // Get header token
    const authHeader = req.headers["authorization"];
    let token: string | null = null;

    if (authHeader?.startsWith("Bearer ")) {
      token = authHeader.substring(7, authHeader.length);
    }

    if (_.isUndefined(token) || _.isNil(token) || token == null) {
      // TODO Forbidden
      throw new ApplicationException("Nenhum token encontrado");
    }

    req.context = {
      ...req.context,
      token,
    };

    // Check token
    const resultToken = await resolveToken(token);
    if (_.isUndefined(resultToken) || _.isNil(resultToken)) {
      throw new ApplicationException("Token inválido");
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore TODO testar
    return resultToken?.id;
  }

  async getContext(req: CustomRequest): Promise<ContextRequest> {
    LogUtil.coloring("SERVER:CONTEXT", `[${req.method}] - ${req.originalUrl} - getContext`);
    if (req?.context) {
      return req?.context;
    }

    throw new ApplicationException("Nenhum contexto");
  }

  async getUserContext(req: CustomRequest): Promise<User> {
    LogUtil.coloring("SERVER:CONTEXT", `getUserContext`);

    if (req?.context?.user) {
      return req?.context?.user;
    }

    const idUserContext = await getUserContextId(req);
    const user = await User.findByPk(idUserContext);
    if (!user) {
      throw new ApplicationException("Precisa estar logado", HTTPResponseCode.Unauthorized);
    }

    Object.assign(req.context, { user });

    return user;
  }
})();
