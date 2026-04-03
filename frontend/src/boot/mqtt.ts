import { defineBoot } from '#q-app/wrappers';
import type { MqttClient } from 'mqtt';
import { connect } from 'mqtt';
import { CookieType } from 'src/common/utils/cookieUtils';
import { Cookies } from 'quasar';

export const createMqttClient = (): MqttClient => {
  const token = Cookies.get(CookieType.ACCESS_TOKEN) ?? '';

  return connect(process.env.MQTT_API, {
    password: token,
  });
};

export default defineBoot(({ app }) => {
  console.log('[INIT] - Loading mqtt', app);
});
