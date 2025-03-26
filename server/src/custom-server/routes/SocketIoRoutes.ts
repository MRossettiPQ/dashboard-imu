import { CustomExpress } from "../core/utils/ExpressUtil";
import { Server, Socket } from "socket.io";
import { DefaultEventsMap } from "socket.io/dist/typed-events";

enum UserType {
  CLIENT = "CLIENT",
  SENSOR = "SENSOR",
}

enum MeasurementType {
  GYROSCOPE = "GYROSCOPE",
}

interface LoginVo {
  ip: string;
  name: string;
  type: UserType;
}

interface GyroscopeMeasurementVo {
  sensorName: string;
  numberMensuration: number;
  hourMensuration: Date;
  Acc_X: number;
  Acc_Y: number;
  Acc_Z: number;
  AccelX_mss: number;
  AccelY_mss: number;
  AccelZ_mss: number;
  Gyr_X: number;
  Gyr_Y: number;
  Gyr_Z: number;
  Mag_X: number;
  Mag_Y: number;
  Mag_Z: number;
  Roll: number;
  Pitch: number;
  Yaw: number;
  Euler_X: number;
  Euler_Y: number;
  Euler_Z: number;
  Quaternion_X: number;
  Quaternion_Y: number;
  Quaternion_Z: number;
  Quaternion_W: number;
}

interface MeasurementVo {
  origin: string;
  type: MeasurementType;
  measurements: GyroscopeMeasurementVo[];
}

interface ExtendedSocket extends Socket {
  type: UserType;
}

class Sensor {
  ip: string;
  name: string;

  constructor(ip: string, name: string) {
    this.ip = ip;
    this.name = name;
  }
}

export default class SocketRoutes {
  io: Server<DefaultEventsMap, DefaultEventsMap>;
  sensors: Map<Sensor, ExtendedSocket> = new Map();
  clients: Map<string, Set<string>> = new Map();

  constructor(app: CustomExpress, io: Server<DefaultEventsMap, DefaultEventsMap>) {
    // Evento de conexão do ‘Socket’.IO
    io.on("connection", (socket: ExtendedSocket): void => {
      socket.on("login", (loginVo: LoginVo): void => {
        socket.type = loginVo.type;
        console.log(loginVo);

        switch (socket.type) {
          case UserType.CLIENT: {
            // Adiciona o cliente ao mapa de clientes com a sua lista de sensores associados
            this.clients.set(socket.id, new Set<string>());
            this.updateSensorList();

            // Adiciona o cliente à sua própria sala
            socket.join(socket.id);

            socket.on("select-sensor", (sensorIp: string): void => {
              // Adiciona o sensor selecionado à lista de sensores associados ao cliente
              const sensorsSet = this.clients.get(socket.id);
              if (sensorsSet) {
                sensorsSet.add(sensorIp);
              }

              // Envia uma mensagem ao sensor para informá-lo de que foi selecionado pelo cliente
              const keys = Array.from(this.sensors.keys());
              const sensor = keys.find((sensor) => sensor.ip === sensorIp);
            });
            break;
          }
          case UserType.SENSOR: {
            const sensor = new Sensor(loginVo.ip, loginVo.name);
            this.sensors.set(sensor, socket);
            this.updateSensorList();

            socket.on("measurements", (measurement: MeasurementVo): void => {
              console.log("Mensagem recebida:", measurement);

              // Envia a mensagem recebida de volta para todos os clientes conectados
              io.emit("measurements", measurement);
            });
            break;
          }
          default:
            break;
        }
      });

      // Evento de desconexão do cliente
      socket.on("disconnect", (): void => {
        console.log(`Um cliente se desconectou - ${socket.id}`);
        console.log(this.clients);

        switch (socket.type) {
          case UserType.CLIENT: {
            // Remove o cliente e os seus sensores associados dos mapas
            const client: Set<string> = this.clients.get(socket.id);

            break;
          }
          case UserType.SENSOR: {
            break;
          }
          default:
            break;
        }
      });
    });
  }

  sendErrorMessage(): void {}

  updateSensorList(): void {
    this.io.emit("sensor-list", this.sensors.values());
  }
}
