import { CustomRequest, CustomResult } from "../../../core/utils/ExpressUtil";
import Result from "../../../core/utils/ResultContent";
import { ApplicationException, HTTPResponseCode } from "../../../core/utils/RequestUtil";
import Session from "../models/Session";
import { getUserContextId } from "../../../core/utils/ContextUtil";
import { getAllCalc, SessionResultVo } from "../services/SciLabServices";
import Procedure from "./Procedure";
import Movement from "../models/Movement";
import Sensor from "../models/Sensor";
import GyroMeasurement from "../models/GyroMeasurement";

export default {
  async list(req: CustomRequest): Promise<CustomResult<Session[]>> {
    const { id: idPatient } = req.params;
    const { rpp, page, field } = req.query;

    if (!idPatient) {
      throw new ApplicationException("Usúario não encontrado", HTTPResponseCode.NotFound);
    }

    // const pagination = await PaginationUtil(Session, {
    //   rpp,
    //   page,
    //   field,
    //   order: [['id', 'ASC']],
    // })
    const pagination = await Session.findAll();

    if (!pagination) {
      throw new ApplicationException("Sessão não encontrada", HTTPResponseCode.NotFound);
    }

    return new Result<Session[]>().withContent(pagination).ok();
  },
  async get(req: CustomRequest): Promise<CustomResult<Session>> {
    const { id } = req.params;
    const session = await Session.findByPk(id);

    if (!session) {
      throw new ApplicationException("Sessão não encontrada");
    }

    return new Result<Session>().withContent(session).ok();
  },
  async save(req: CustomRequest): Promise<CustomResult<SessionResultVo>> {
    const userId = await getUserContextId(req);
    const { session } = req.body;

    const newSession = await Session.create(
      {
        ...session,
        userId,
      },
      {
        include: [
          {
            model: Movement,
            include: [
              {
                model: Sensor,
                include: [
                  {
                    model: GyroMeasurement,
                  },
                ],
              },
            ],
          },
        ],
      },
    );

    const result = await getAllCalc(newSession.movements);

    return new Result<SessionResultVo>()
      .withMessage("Sessão salva com sucesso!")
      .withContent({
        session,
        result,
      })
      .ok();
  },
  async mensurationList(req: CustomRequest): Promise<CustomResult<GyroMeasurement[]>> {
    const { sessionId, movementId } = req.params;
    const { rpp, page, field } = req.query;

    const pagination = await GyroMeasurement.findAll({
      order: [
        ["numberMensuration", "ASC"],
        ["sensorId", "ASC"],
      ],
      include: [
        {
          model: Sensor,
          include: [
            {
              model: Movement,
              where: {
                sessionId,
              },
            },
          ],
        },
      ],
    });

    if (!pagination) {
      throw new ApplicationException("Nada encontrado", HTTPResponseCode.NotFound);
    }

    return new Result<GyroMeasurement[]>().withMessage("Sessão salva com sucesso!").withContent(pagination).ok();
  },
  async metadata(): Promise<CustomResult<Map<string, unknown>>> {
    const metadata = new Map<string, unknown[]>();
    metadata.set("procedures", Procedure.list());
    return new Result<Map<string, unknown>>().withContent(metadata).ok();
  },
};
