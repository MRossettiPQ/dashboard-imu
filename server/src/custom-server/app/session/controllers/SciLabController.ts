import { CustomRequest, CustomResult } from "../../../core/utils/ExpressUtil";
import { ApplicationException, HTTPResponseCode } from "../../../core/utils/RequestUtil";
import Session from "../models/Session";
import { getUserContextId } from "../../../core/utils/ContextUtil";
import { getAllCalc, MovementResult } from "../services/SciLabServices";
import Result from "../../../core/utils/ResultContent";
import Movement from "../models/Movement";
import Sensor from "../models/Sensor";
import GyroMeasurement from "../models/GyroMeasurement";

export default {
  async calculationVariabilityCenter(req: CustomRequest): Promise<CustomResult<any>> {
    const userId = await getUserContextId(req);
    const { id: sessionId } = req.params;

    if (!userId) {
      throw new ApplicationException(
        "Não foi encontrado um usúario logado",
        HTTPResponseCode.Forbidden,
      );
    }

    if (!sessionId) {
      throw new ApplicationException(
        "Não foi encontrado o id da sessão",
        HTTPResponseCode.BadRequest,
      );
    }

    const session = await Session.findByPk(sessionId);
    const movements = await Movement.findAll({
      where: {
        sessionId,
      },
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
    });

    if (!movements?.length) {
      throw new ApplicationException(
        "Nenhuma medição encontrada para o id da sessão",
        HTTPResponseCode.BadRequest,
      );
    }

    const result = await getAllCalc(movements);

    return new Result<MovementResult[]>().withContent(result).ok();
  },
};
