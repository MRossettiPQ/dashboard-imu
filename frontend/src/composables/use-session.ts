import { computed, onMounted, onUnmounted, ref } from 'vue';

import { createProvidedComposable } from '.';
import { useRoute, useRouter } from 'vue-router';
import { createMqttClient } from 'boot/mqtt';
import type { ErrorWithReasonCode, MqttClient } from 'mqtt';
import { notify } from 'src/common/utils/NotifyUtils';
import { api } from 'boot/axios';
import { SessionState } from 'src/common/api/manual/constructors_api';
import type { PatientDto, SensorInfo } from 'src/common/api/generated/models';
import { SessionType } from 'src/common/api/generated/models';

const kUseSessionEditor = Symbol('useSessionEditor');

export const { useSessionEditor, useProvidedSessionEditor, provideSessionEditor } =
  createProvidedComposable(kUseSessionEditor, 'sessionEditor', () => {
    const mqttClient = ref<MqttClient | null>(null);

    const session = ref<SessionState>(new SessionState());
    const patient = ref<PatientDto>();
    const router = useRouter();
    const route = useRoute();
    const loading = ref<boolean>(false);
    const error = ref<boolean>(false);
    const patientUuid = computed(() => route.params?.['uuid']?.toString());
    const subscribeMap = ref<Map<string, string>>(new Map<string, string>());
    const availableSensors = ref<Set<SensorInfo>>(new Set<SensorInfo>());

    // Medições em progresso
    const inProgress = ref(false);
    // Ao finalizar sessão
    const loadingSave = ref(false);

    function mqttErrorHandler(err: Error | ErrorWithReasonCode) {
      console.error('Erro MQTT:', err);
      notify.error('Falha na conexão em tempo real com os sensores.');
    }

    function mqttMessageRouter(topic: string, message: Buffer) {
      const data: unknown = JSON.parse(message.toString());

      // Medições dos sensores
      // Tópico: session/{id}/sensor/{mac}/measurement
      if (topic.match(/^session\/[^/]+\/sensor\/[^/]+\/measurement$/)) {
        // data = { originIdentifier, content: [{ accelX, accelY, ... }] }
        // Aqui você acumula ou renderiza em gráfico real-time
        handleMeasurements(data);
        return;
      }

      // Lista de sensores disponíveis (broadcast do backend)
      // Tópico: sensors/available
      if (topic === 'sensors/available') {
        // availableSensors.value = data.sensors;
        return;
      }
    }

    function handleMeasurements(data: unknown) {
      console.log('Dados recebidos:', data);
    }

    function mqttOnConnect() {
      const sessionId = session.value.getId;
      console.log('MQTT Conectado! Inscrevendo-se na sessão:', sessionId);

      mqttClient.value?.subscribe('sensors/available', (err) => {
        if (!err) {
          console.log('Inscrição confirmada na sala de espera.');
        } else {
          console.error('Erro ao se inscrever no tópico', err);
        }
      });

      // Subscribe to all messages for this specific session
      mqttClient.value?.subscribe(`session/${sessionId}/#`, (err) => {
        subscribeMap.value.set('session', sessionId);
        if (!err) {
          console.log('Inscrição confirmada na sala de espera.');
        } else {
          console.error('Erro ao se inscrever no tópico', err);
        }
      });
    }

    onUnmounted(() => {
      if (mqttClient.value) {
        if (session.value.getId) {
          mqttClient.value.unsubscribe(`session/${session.value.getId}/#`);
        }
        mqttClient.value.end();
      }
    });

    onMounted(async () => {
      if (!patientUuid.value) {
        // uuid is the patientId from route params
        notify.error('Obrigatório o identificador da sessão');
        return await router.push({ name: 'private.session' });
      }

      try {
        console.log(availableSensors.value);
        loading.value = true;

        const [{ data: patientData }, { data: sessionData }] = await Promise.all([
          api.getApiPatientsUuid(patientUuid.value),
          api.postApiSessionsCreate({
            patientId: patientUuid.value,
            type: SessionType.REAL,
          }),
        ]);

        patient.value = patientData.content;
        session.value.assignValue(sessionData.content);

        mqttClient.value = createMqttClient();
        mqttClient.value.on('connect', mqttOnConnect);
        mqttClient.value.on('message', mqttMessageRouter);
        mqttClient.value.on('error', mqttErrorHandler);
      } catch (e: unknown) {
        error.value = true;
        loading.value = false;
        notify.error(`Erro inesperado ao iniciar sessão ${(e as Error).message}`);
        return await router.push({ name: 'private.session' });
      } finally {
        loading.value = false;
      }
    });

    async function addSensor(sensorInfo: SensorInfo): Promise<void> {
      const {
        data: { content },
      } = await api.postApiSessionsSessionIdSensorsMacAddress(
        session.value.getId,
        sensorInfo.macAddress!,
      );
      console.log('Sensor adicionado:', content);
    }

    async function startCommand(): Promise<void> {
      await api.postApiSessionsSessionIdStart(session.value.getId);
    }

    async function stopCommand(): Promise<void> {
      await api.postApiSessionsSessionIdStop(session.value.getId);
    }

    async function finalize(): Promise<void> {
      await api.postApiSessionsSessionIdFinalize(session.value.getId);
    }

    return {
      session,
      error,
      loading,
      patient,
      mqttClient,
      inProgress,
      loadingSave,
      subscribeMap,
      addSensor,
      stopCommand,
      startCommand,
      finalize,
    };
  });
