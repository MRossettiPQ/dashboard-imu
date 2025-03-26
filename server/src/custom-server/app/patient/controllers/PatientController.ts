import { CustomRequest, CustomResult } from "../../../core/utils/ExpressUtil";
import Result, { Pagination } from "../../../core/utils/ResultContent";
import { ApplicationException } from "../../../core/utils/RequestUtil";
import Patient from "../models/Patient";

export default {
  async list(req: CustomRequest): Promise<CustomResult<Patient>> {
    const { rpp, page, field, term } = req.query;
    const { rows, count } = await Patient.findAndCountAll();

    return new Pagination<Patient>().withPagination(rows, count, page?.toString(), rpp?.toString()).ok();
  },
  async get(req: CustomRequest): Promise<CustomResult<Patient>> {
    const { id } = req.params;
    const user = await Patient.findByPk(id);

    if (!user) {
      throw new ApplicationException("Usuario não encontrado");
    }

    return new Result<Patient>().withContent(user).ok();
  },
  async save(req: CustomRequest): Promise<CustomResult<Patient>> {
    let patient = new Patient(req.body);

    patient = await patient.save();
    return new Result<Patient>().withContent(patient).ok();
  },
};
