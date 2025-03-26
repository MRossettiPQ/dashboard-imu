import User, { UserRole } from "../models/User";
import { CustomRequest, CustomResult } from "../../../core/utils/ExpressUtil";
import Result from "../../../core/utils/ResultContent";
import { hashSync } from "bcryptjs";
import { ApplicationException } from "../../../core/utils/RequestUtil";
import { compareCrypt, createToken } from "../../../core/middleware/AuthorizeJwt";
import { getUserContext } from "../../../core/utils/ContextUtil";

interface LoginVo {
  username: string;
  password: string;
}

interface AccessUserVo {
  id: number;
  uuid?: string;
  username: string;
  name: string;
  email: string;
  role: UserRole;
  accessToken?: string;
}

export default {
  async register(req: CustomRequest): Promise<CustomResult<AccessUserVo>> {
    const newUser = await User.create({
      ...req.body,
      password: hashSync(req.body.password, 8),
    });

    const token: string = await createToken({ id: newUser.id });
    if (!token) {
      throw new ApplicationException("Não foi possivel autenticar o usuario");
    }

    return new Result<AccessUserVo>()
      .withContent({
        id: newUser.id,
        uuid: newUser.uuid,
        name: newUser.name,
        username: newUser.username,
        email: newUser.email,
        role: newUser.role,
        accessToken: token,
      })
      .withMessage("Usuario logado com sucesso!")
      .ok();
  },
  async login(req: CustomRequest): Promise<CustomResult<AccessUserVo>> {
    const { username, password }: LoginVo = req.body;
    const userFound: User = await User.findOne({
      where: {
        username,
      },
    });

    if (!userFound) {
      throw new ApplicationException("Usuario não encontrado");
    }

    const validPassword: boolean = await compareCrypt(password, userFound.password);

    if (!validPassword) {
      throw new ApplicationException("Senha inválida");
    }

    const token: string = await createToken({ id: userFound.id });

    if (!token) {
      throw new ApplicationException("Não foi possivel autenticar o usuario");
    }

    return new Result<AccessUserVo>()
      .withContent({
        id: userFound.id,
        uuid: userFound.uuid,
        name: userFound.name,
        role: userFound.role,
        username: userFound.username,
        email: userFound.email,
        accessToken: token,
      })
      .withMessage("Usuario registrado com sucesso!")
      .ok();
  },
  async context(req: CustomRequest): Promise<CustomResult<AccessUserVo>> {
    const userContext: User = await getUserContext(req);

    if (!userContext) {
      throw new ApplicationException("Usuario não encontrado");
    }

    return new Result<AccessUserVo>()
      .withContent({
        id: userContext.id,
        uuid: userContext.uuid,
        name: userContext.name,
        role: userContext.role,
        username: userContext.username,
        email: userContext.email,
      })
      .ok();
  },
};
