import { CustomResult } from "../../../core/utils/ExpressUtil";
import ResultContent from "../../../core/utils/ResultContent";
import dayjs from "dayjs";

interface PingVo {
  dateTime: string;
  env: string;
}

export default {
  async ping(): Promise<CustomResult<PingVo>> {
    return new ResultContent<PingVo>()
      .withContent({
        dateTime: dayjs().toISOString(),
        env: "Development",
      })
      .withMessage("Teste")
      .build();
  },
};
