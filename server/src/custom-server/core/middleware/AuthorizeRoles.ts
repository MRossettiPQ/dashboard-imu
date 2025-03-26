import { CustomRequest } from "../utils/ExpressUtil";

import LogUtil from "../utils/LogUtil";
import { getUserContext } from "../utils/ContextUtil";
import { UserRole } from "../../app/user/models/User";
import { ApplicationException, HTTPResponseCode } from "../utils/RequestUtil";

export const { verifyRoles } = new (class AuthorizeRoles {
  verifyRoles(roles: UserRole[] = []) {
    return async function (req: CustomRequest): Promise<void> {
      LogUtil.coloring("SERVER:VERIFY-ROLES", "Verificar Roles do Usuário");
      const userContext = await getUserContext(req);

      LogUtil.coloring("SERVER:VERIFY-ROLES", `${roles} ${userContext}`);
      // Implement the middleware function based on the options object
      if (!roles.includes(userContext?.getDataValue?.("role")) || !userContext) {
        throw new ApplicationException(
          "Usuário não foi autorizado",
          HTTPResponseCode.Forbidden,
        );
      }
    };
  }
})();
