import { defineBoot } from '#q-app/wrappers';
import type { ManagerOptions, Socket, SocketOptions } from 'socket.io-client';
import { io } from 'socket.io-client';
import type { EventsMap } from '@socket.io/component-emitter';
import { Cookies } from 'quasar';
import { CookieType } from 'src/common/utils/cookieUtils';

declare module 'vue' {
  interface ComponentCustomProperties {
    $socket: Socket<EventsMap, EventsMap>;
  }
}

const wss = process.env.SOCKET_API;
const opts: Partial<ManagerOptions & SocketOptions> = {
  reconnectionDelayMax: 10000,
  port: 8001,
  autoConnect: false,
  reconnectionAttempts: 3,
  reconnection: false,
  query: {
    type: 'USER',
    token: Cookies.get(CookieType.ACCESS_TOKEN),
  },
};

const socket: Socket<EventsMap, EventsMap> = io(wss, opts);
socket.on('connect_error', (error) => {
  console.error('Erro de conexão:', error.message);
});
export default defineBoot(({ app }) => {
  console.log('[INIT] - Loading socket');
  app.config.globalProperties.$socket = socket;
});

export { socket };
