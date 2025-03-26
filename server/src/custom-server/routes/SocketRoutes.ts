import { CustomExpress } from "../core/utils/ExpressUtil";
import { Instance } from "express-ws";

export default function SocketRoutes(app: CustomExpress, expressWs: Instance): void {
  // TODO WebSocket
  // app.ws("/socket", WebSocketController.sensorConnection(expressWs));
}
