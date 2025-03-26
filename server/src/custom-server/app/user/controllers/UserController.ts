import User from "../models/User";
import { CustomRequest, CustomResult } from "../../../core/utils/ExpressUtil";
import Result from "../../../core/utils/ResultContent";
import { ApplicationException } from "../../../core/utils/RequestUtil";

export default {
  async get(req: CustomRequest): Promise<CustomResult<User>> {
    console.log("get", req.params);
    const { id } = req.params;
    const user: User = await User.findByPk(id);

    if (!user) {
      throw new ApplicationException("Usuario não encontrado");
    }

    return new Result<User>().withContent(user).ok();
  },
  async save(req: CustomRequest): Promise<CustomResult<User>> {
    const body: User = req.body;
    let user: User = await User.findByPk(body.id);

    if (!user) {
      throw new ApplicationException("Usuario não encontrado");
    }

    user = await user.update(req.body);

    return new Result<User>().withContent(user).ok();
  },
};
