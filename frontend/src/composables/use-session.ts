import { computed, onMounted, onUnmounted, ref } from 'vue';

import { createProvidedComposable } from '.';
import { useRoute, useRouter } from 'vue-router';
import { createMqttClient } from 'boot/mqtt';
import type { MqttClient } from 'mqtt';
import { notify } from 'src/common/utils/NotifyUtils';
import { api } from 'boot/axios';
import type { PatientDto } from 'src/common/api/generated/models';
import { SessionType } from 'src/common/api/generated/models';
import { Session } from 'src/common/api/manual/constructors_api';

const kUseSessionEditor = Symbol('useSessionEditor');

export const { useSessionEditor, useProvidedSessionEditor, provideSessionEditor } =
  createProvidedComposable(kUseSessionEditor, 'sessionEditor', () => {
    const mqttClient = ref<MqttClient | null>(null);

    const session = ref<Session>(new Session());
    const patient = ref<PatientDto>();
    const router = useRouter();
    const route = useRoute();
    const sensors = ref<[]>();
    const nodes = ref<[]>();
    const loading = ref<boolean>(false);
    const error = ref<boolean>(false);
    const uuid = computed(() => route.params?.['uuid']?.toString());

    onUnmounted(() => {
      if (mqttClient.value) {
        if (sessionId.value) {
          mqttClient.value.unsubscribe(`session/${sessionId.value}/#`);
        }
        mqttClient.value.end();
      }
    });

    onMounted(async () => {
      if (!uuid.value) {
        // uuid is the patientId from route params
        notify.error('Obrigatório o identificador da sessão');
        return await router.push({ name: 'private.session' });
      }

      try {
        loading.value = true;

        // 1. Fetch Patient and Procedures
        const { data } = await api.getApiPatientsUuid(uuid.value);
        const [{ data: patientData }, { data: sessionData }] = await Promise.all([
          api.getApiPatientsUuid(uuid.value),
          api.postApiSessionsCreate({ patientId: uuid.value, type: SessionType.REAL }),
        ]);

        if (data.content) {
          patient.value = patientData.content;
        }

        // 2. Call backend REST API to create the Session (Optional but recommended)
        // You need an endpoint like POST /api/sessions { patientId: uuid.value }
        // that returns the new session UUID.
        // const sessionRes = await api.postApiSessions({ patientId: uuid.value });
        // sessionId.value = sessionRes.data.id;

        // TEMPORARY: For testing, generating a fake session ID if you don't have the endpoint yet
        session.value.id = sessionData.content.id!;
        session.value.type = sessionData.content.type!;

        // 3. Connect to MQTT
        mqttClient.value = createMqttClient();
        mqttClient.value.on('connect', () => {
          console.log('MQTT Conectado! Inscrevendo-se na sessão:', session.value.getId);

          // Subscribe to all messages for this specific session
          mqttClient.value?.subscribe(`session/${session.value.getId}/#`, (err) => {
            if (!err) {
              console.log('Inscrição confirmada na sala de espera.');
            } else {
              console.error('Erro ao se inscrever no tópico', err);
            }
          });
        });

        // 4. Handle incoming MQTT messages
        mqttClient.value.on('message', (topic, message) => {
          // Convert Buffer to string, then parse JSON
          const payloadStr = message.toString();
          let data: unknown;
          try {
            data = JSON.parse(payloadStr);
          } catch (e) {
            console.error('Erro ao parsear payload MQTT', e, payloadStr);
            return;
          }

          console.log(`[MQTT] Recebido no tópico: ${topic}`, data);
        });

        mqttClient.value.on('error', (err) => {
          console.error('Erro MQTT:', err);
          notify.error('Falha na conexão em tempo real com os sensores.');
        });
      } catch (e: unknown) {
        error.value = true;
        loading.value = false;
        notify.error(`Erro inesperado ao iniciar sessão ${(e as Error).message}`);
        return await router.push({ name: 'private.session' });
      } finally {
        loading.value = false;
      }
    });

    return {};
  });
