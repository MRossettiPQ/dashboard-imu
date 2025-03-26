import { CustomExpress } from "../core/utils/ExpressUtil";
import { asyncHandler, middlewareHandler } from "../core/utils/RequestUtil";
import UserController from "../app/user/controllers/UserController";
import { verifyToken } from "../core/middleware/AuthorizeJwt";
import { verifyRoles } from "../core/middleware/AuthorizeRoles";
import { UserRole } from "../app/user/models/User";
import { verifyExistsCPFinPatient } from "../core/middleware/RegisterValidation";
import PatientController from "../app/patient/controllers/PatientController";
import SessionController from "../app/session/controllers/SessionController";
import SciLabController from "../app/session/controllers/SciLabController";

export default function PrivateRoutes(app: CustomExpress): void {
  // Usuários
  app.get("/api/users/:id", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR])]), asyncHandler(UserController.get));
  app.post("/api/users", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR])]), asyncHandler(UserController.save));

  // Pacientes
  app.get("/api/patients", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]), asyncHandler(PatientController.list));
  app.get("/api/patients/:id", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]), asyncHandler(PatientController.get));
  app.post(
    "/api/patients",
    middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST]), verifyExistsCPFinPatient]),
    asyncHandler(PatientController.save),
  );

  // Sessão
  app.get("/api/patients/:id/sessions", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]), asyncHandler(SessionController.list));
  app.get("/api/sessions/:id", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]), asyncHandler(SessionController.get));
  app.get(
    "/api/sessions/:sessionId/movement/:movementId",
    middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]),
    asyncHandler(SessionController.mensurationList),
  );
  app.post("/api/sessions", middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]), asyncHandler(SessionController.save));

  // SciLab
  app.get(
    "/api/sessions/:id/movements/:movementId/scilab",
    middlewareHandler([verifyToken, verifyRoles([UserRole.ADMINISTRATOR, UserRole.PHYSIOTHERAPIST])]),
    asyncHandler(SciLabController.calculationVariabilityCenter),
  );
}
