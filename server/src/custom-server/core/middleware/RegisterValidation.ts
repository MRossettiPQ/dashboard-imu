import { CustomNextFunction, CustomRequest, CustomResponse } from "../utils/ExpressUtil";
import { ApplicationException } from "../utils/RequestUtil";
import User, { UserAttributes } from "../../app/user/models/User";
import Patient from "../../app/patient/models/Patient";

export const { verifyUserEmailDuplicate, verifyExistsCPFinPatient } = new (class RegisterValidation {
  async verifyUserEmailDuplicate(req: CustomRequest): Promise<void> {
    const register: UserAttributes = req.body;

    if (!register) {
      throw new ApplicationException("Nenhum dado valido foi enviado");
    }

    const { username, email } = register;

    const userFound: User = await User.findOne({
      where: {
        username,
      },
    });

    if (userFound) {
      throw new ApplicationException("Falhou! Nome de usuario esta em uso!");
    }

    const emailFound: User = await User.findOne({
      where: {
        email,
      },
    });

    if (emailFound) {
      throw new ApplicationException("Falhou! E-mail esta em uso!");
    }
  }

  async verifyExistsCPFinPatient(req: CustomRequest, _res: CustomResponse, next: CustomNextFunction): Promise<void> {
    if (!req.body) {
      throw new ApplicationException("Nenhum dado valido foi enviado");
    }

    const { cpf, id } = req.body;
    if (id) {
      next();
      return;
    } else {
      const patientFound: Patient = await Patient.findOne({
        where: {
          cpf,
        },
      });

      if (patientFound) {
        throw new ApplicationException("CPF já está em uso");
      }

      next();
    }
  }
})();
