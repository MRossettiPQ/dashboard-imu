import settings from "../../settings";
import { Sequelize } from "sequelize-typescript";
import glob from "glob";
import Log, { TextColor } from "../utils/LogUtil";
import { ApplicationException } from "../utils/RequestUtil";
import { resolve } from "path";
import User from "../../app/user/models/User";
import Session from "../../app/session/models/Session";
import Patient from "../../app/patient/models/Patient";
import Sensor from "../../app/session/models/Sensor";
import Movement from "../../app/session/models/Movement";
import GyroMeasurement from "../../app/session/models/GyroMeasurement";

export default new (class CustomDatabase {
  sequelize?: Sequelize;
  sqliteExists?: boolean;

  async init(): Promise<void> {
    try {
      Log.coloring("SERVER:DATABASE", __dirname);
      Log.coloring("SERVER:DATABASE", settings.cacheDir);
      Log.coloring(
        "SERVER:DATABASE",
        resolve(settings.cacheDir, settings.database.database),
      );

      this.sequelize = new Sequelize({
        ...settings.database.sequelize["sqlite"],
        storage: resolve(settings.cacheDir, settings.database.database),
        dialect: "sqlite",
        benchmark: false,
        logging: false,
        models: [User, Session, Sensor, Patient, Movement, GyroMeasurement],
      });

      Log.coloring("SERVER:DATABASE", "Limpar ou Rsync no banco");
      const { alter, wipe_on_start } = settings.database.syncOptions;
      const force: boolean = wipe_on_start || !this.sqliteExists;
      await this.sequelize.sync({ alter, force });
    } catch (e) {
      Log.coloring("SERVER:DATABASE", "Error ao limpar ou rsync no banco", TextColor.Red);
      Log.coloring("SERVER:DATABASE", e.message, TextColor.Red);
      throw new ApplicationException(e.message);
    }
  }

  async models(): Promise<void> {
    const dialect = settings.database.dialect;
    if (!dialect || dialect != "sqlite") {
      throw new ApplicationException("Dialect não foi descrito no .env");
    }

    // console.log(
    //   glob.sync(`../src/custom-server/app/**/*.ts`, {
    //     cwd: settings.cacheDir,
    //     absolute: true,
    //   }),
    // );

    if (dialect == "sqlite") {
      const cacheFolder: string[] = glob.sync(`**.sqlite`, {
        cwd: settings.cacheDir,
        absolute: true,
      });
      this.sqliteExists = !!cacheFolder.length;
      if (!this.sqliteExists) {
        Log.coloring("SERVER:SEQUELIZE-SQLITE", "Banco não existe");
      } else {
        Log.coloring("SERVER:SEQUELIZE-SQLITE", cacheFolder[0]);
      }
    }
  }
})();
