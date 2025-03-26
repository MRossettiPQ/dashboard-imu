import Database from "./core/database";
import settings from "./settings";
import { CustomExpress } from "./core/utils/ExpressUtil";
import routes from "./routes";
import express from "express";
import * as bodyParser from "body-parser";
import cors from "cors";
import Log, { TextColor } from "./core/utils/LogUtil";
import http from "http";
import { Server } from "socket.io";
import { DefaultEventsMap } from "socket.io/dist/typed-events";

interface ICustomNodeServer {
  app?: CustomExpress;
  // database: CustomDatabase;
  loading: boolean;
  started: boolean;
  init: () => Promise<void>;
  listen: () => Promise<void>;
  readonly port: number;
}

class CustomNodeServer implements ICustomNodeServer {
  app?: CustomExpress;
  server?: http.Server<typeof http.IncomingMessage, typeof http.ServerResponse>;
  socketIo?: Server<DefaultEventsMap, DefaultEventsMap>;

  loading = false;
  started = false;
  database = Database;

  get port(): number {
    return settings.host.port;
  }

  async init(): Promise<void> {
    try {
      this.loading = true;

      Log.coloring(
        "SERVER",
        "Inicializando aplicação, e carregando models",
        TextColor.Blue,
      );
      await this.database.models();

      // Instance express
      this.app = express();
      this.server = http.createServer(this.app);

      // Database initialization
      Log.coloring("SERVER", "Iniciando conexão com banco de dados");
      await this.database.init();

      // Cors rules
      this.app.use(cors());

      // Folder containing the SPA project after the build is performed
      this.app.use(express.static(__dirname + "/public"));

      // Parse requests of content-type - application/json
      this.app.use(bodyParser.json({ limit: "50mb" }));

      // Parse requests of content-type - application/x-www-form-urlencoded
      this.app.use(bodyParser.urlencoded({ extended: true }));

      // Active web-socket on app express
      // const expressWs = enableWs(this.app)

      // Configurando o Socket.IO
      this.socketIo = new Server(this.server);

      // Morgan logging
      // this.app.use(morgan(Log.logMiddleware));

      // Only after models are loaded
      Log.coloring("SERVER", "Carregando controllers da aplicacao");
      routes(this.app, this.socketIo);

      // Listen server in port
      await this.listen();
      Log.coloring(
        "SERVER",
        `Aplicação iniciada na porta ${this.port} do host`,
        TextColor.Blue,
      );

      this.started = true;
    } catch (e: unknown) {
      Log.coloring("SERVER", "Ocorreu um erro ao executar o servidor", TextColor.Red);
      console.log(e);
    } finally {
      this.loading = false;
    }
  }

  async listen(): Promise<void> {
    // Iniciar e tratar erros do express
    return new Promise((resolve, reject) => {
      if (!this.server) {
        return reject();
      }

      this.server = this.server.listen(this.port);

      // Erros da aplicação no geral
      process.once("uncaughtException", reject);
      // Erros da instancia do server
      this.server.once("error", reject);
      this.server.once("listening", resolve);
    });
  }
}

export default CustomNodeServer;
