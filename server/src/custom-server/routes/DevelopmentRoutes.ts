import { CustomExpress } from "../core/utils/ExpressUtil";
import { asyncHandler } from "../core/utils/RequestUtil";
import DevelopmentController from "../app/development/controllers/DevelopmentController";

export default function DevelopmentRoute(app: CustomExpress): void {
  app.get("/ping", asyncHandler(DevelopmentController.ping));
}
