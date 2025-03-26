import { CustomExpress } from "../core/utils/ExpressUtil";
import { asyncHandler, middlewareHandler } from "../core/utils/RequestUtil";
import AuthenticationController from "../app/user/controllers/AuthenticationController";
import { verifyUserEmailDuplicate } from "../core/middleware/RegisterValidation";
import SessionController from "../app/session/controllers/SessionController";

export default function PublicRoutes(app: CustomExpress): void {
  app.post(
    "/api/authentication/register",
    middlewareHandler(verifyUserEmailDuplicate),
    asyncHandler(AuthenticationController.register),
  );
  app.post("/api/authentication/login", asyncHandler(AuthenticationController.login));
  app.get("/api/authentication/context", asyncHandler(AuthenticationController.context));

  // TODO Metadata - list sensors and metadata socket
  app.get("/api/session/metadata", asyncHandler(SessionController.metadata));
  // app.get('/api/websocket/metadata', asyncHandler(WebSocketController.metadata))
  // app.get('/api/websocket/list', asyncHandler(WebSocketController.listSensor))
}
