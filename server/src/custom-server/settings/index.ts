import { resolve } from "path";

let dirName: string = __dirname;
if (process.env.NODE_ENV?.toUpperCase() != "DEVELOPMENT") {
  // dirName = dirName.replace(/([\\/])((app\.asar)|(resources))/g, "");
  dirName = resolve(dirName, "../../");
}

export default {
  cacheDir: resolve(dirName, "../../cache/"),
  env_name: "DEVELOPMENT",
  development: true,
  just_api: false,
  secret: "dash-imu-secret-key",
  electron: {
    url: "http://localhost",
  },
  host: {
    port: 8000,
    port_socket: 8001,
    cors: {
      allowedHeaders: [
        "Origin",
        "X-Requested-With",
        "Content-Type",
        "Accept",
        "Authorization",
      ],
      origin: "*",
      methods: ["GET", "HEAD", "OPTIONS", "POST", "PUT"],
    },
    logs: {
      dateFormat: "DD/MM/YYYY[-]HH:mm:ss",
    },
  },
  database: {
    dialect: "sqlite",
    database: "dashboard.sqlite",
    sequelize: {
      sqlite: {
        dialect: "sqlite",
        benchmark: false,
        pool: {
          max: 5,
          min: 0,
          acquire: 900000,
          idle: 100000,
        },
      },
    },
    syncOptions: {
      wipe_on_start: false,
      alter: false,
    },
  },
  swagger: {
    options: {
      explorer: true,
    },
  },
};
