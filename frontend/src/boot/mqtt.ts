import { defineBoot } from '#q-app/wrappers';
import mqtt from 'mqtt'; // <-- Mudança principal aqui
import type { MqttClient } from 'mqtt';
import { CookieType } from 'src/common/utils/cookieUtils';
import { Cookies } from 'quasar';

export const createMqttClient = (): MqttClient => {
  const token = Cookies.get(CookieType.ACCESS_TOKEN) ?? '';

  // Use mqtt.connect() em vez de apenas connect()
  return mqtt.connect(process.env.MQTT_API, {
    username: 'dashboard',
    password: token,
  });
};

export default defineBoot(({ app }) => {
  console.log('[INIT] - Loading mqtt', app);
});
