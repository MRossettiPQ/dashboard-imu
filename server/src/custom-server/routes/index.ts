import settings from "../settings";
import DevelopmentRoutes from "./DevelopmentRoutes";
import Header from "./Header";
import { spaResolver } from "../core/utils/RequestUtil";
import PublicRoutes from "./PublicRoutes";
import PrivateRoutes from "./PrivateRoutes";
import { Server } from "socket.io";
import { DefaultEventsMap } from "socket.io/dist/typed-events";
import { CustomExpress } from "../core/utils/ExpressUtil";
import SocketIoRoutes from "./SocketIoRoutes";

export default function routes(
  app: CustomExpress,
  socketIo?: Server<DefaultEventsMap, DefaultEventsMap>,
  // , expressWs: Instance
): void {
  // console.log(expressWs.getWss().address());
  // SocketRoutes(app, expressWs)
  new SocketIoRoutes(app, socketIo);

  if (settings.development) {
    DevelopmentRoutes(app);
  }

  // Header
  app.use(Header);

  // Redirect to page in spa or api
  if (!settings.just_api) {
    app.use(spaResolver);
  }

  PublicRoutes(app);

  PrivateRoutes(app);
}
