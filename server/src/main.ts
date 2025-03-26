import { app, BrowserWindow, Menu, nativeImage, Notification, shell, Tray } from "electron";
import CustomNodeServer from "./custom-server";
import settings from "./custom-server/settings";
import { join, resolve } from "node:path";

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require("electron-squirrel-startup")) {
  app.quit();
}

function quit(): void {
  if (process.platform !== "darwin") {
    app.quit();
  }
}

function showNotification(title: string, body: string, icon: string): void {
  const notification: Notification = new Notification({
    title,
    body,
    icon,
  });

  notification.on("click", async (event: Event): Promise<void> => await open(event));

  notification.show();
}

async function open(event: Event): Promise<void> {
  event.preventDefault();
  await shell.openExternal(`${settings.electron.url}:${this.server.port}/`);
}

async function createMenu(): Promise<void> {
  // Generate tray icon
  const icon = nativeImage.createFromPath(join(__dirname, "./assets/icon.png"));
  const tray = new Tray(icon);

  // Tray icon menu
  const contextMenu = Menu.buildFromTemplate([
    {
      label: "Open",
      type: "normal",
      // icon: icon.resize({ width: 16, height: 16 }),
      click: () => shell.openExternal(`${settings.electron.url}:${this.server.port}/`),
    },
    {
      type: "separator",
    },
    {
      label: "Close",
      type: "normal",
      click: quit,
    },
  ]);

  tray.setContextMenu(contextMenu);
  tray.setToolTip("Dashboard IMU");
  tray.setTitle("Dashboard IMU");
}

export default (async (): Promise<void> => {
  const server = new CustomNodeServer();

  async function createWindow(): Promise<void> {
    await createMenu();

    // Create the browser window.
    // const mainWindow = new BrowserWindow({
    //   width: 800,
    //   height: 600,
    //   webPreferences: {
    //     preload: join(__dirname, "preload.js"),
    //   },
    // });
    //
    // // Load the index.html of the app.
    // if (MAIN_WINDOW_VITE_DEV_SERVER_URL) {
    //   await mainWindow.loadURL(MAIN_WINDOW_VITE_DEV_SERVER_URL);
    // } else {
    //   const file = `../renderer/${MAIN_WINDOW_VITE_NAME}/index.html`;
    //   await mainWindow.loadFile(join(__dirname, file));
    // }

    if (!server.started) {
      await server.init();

      if (process.env.NODE_ENV?.toUpperCase() != "DEVELOPMENT") {
        await shell.openExternal(`${settings.electron.url}:${server.port}/`);
      }
    }

    showNotification("Dashboard IMU", "Pronto para uso", resolve(__dirname, "./assets/icon.ico"));

    // Open the DevTools.
    // mainWindow.webContents.openDevTools();
  }

  try {
    console.log("\x1b[35m[ELECTRON] - Init\x1b[0m");

    // This method will be called when Electron has finished
    // initialization and is ready to create browser windows.
    // Some APIs can only be used after this event occurs.
    app.on("ready", createWindow);

    // Quit when all windows are closed, except on macOS. There, it's common
    // for applications and their menu bar to stay active until the user quits
    // explicitly with Cmd + Q.
    app.on("window-all-closed", () => {
      if (process.platform !== "darwin") {
        app.quit();
      }
    });

    app.on("activate", () => {
      // On OS X it's common to re-create a window in the app when the
      // dock icon is clicked and there are no other windows open.
      if (BrowserWindow.getAllWindows().length === 0) {
        createWindow();
      }
    });

    // In this file you can include the rest of your app's specific main process
    // code. You can also put them in separate files and import them here.
    console.log("\x1b[35m[ELECTRON] - Initialized\x1b[0m");
  } catch (e) {
    console.log(e);
    process.exit(0);
  }
})();
