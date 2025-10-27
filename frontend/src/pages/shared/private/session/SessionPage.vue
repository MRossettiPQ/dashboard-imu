<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { socket, SocketEvents } from 'boot/socket';
import { MessageSensorListDto } from 'src/common/models/socket/MessageSensorListDto';
import { plainToInstance } from 'class-transformer';
import { useRoute, useRouter } from 'vue-router';
import { patientService } from 'src/common/services/patient/patient-service';
import { notify } from 'src/common/utils/NotifyUtils';
import type { Patient } from 'src/common/models/patient/Patient';
import type { SessionMeasurementDto } from 'src/common/models/socket/SessionMeasurementDto';
import ErrorDiv from 'components/ErrorDiv/ErrorDiv.vue';
import LoadDiv from 'components/LoadDiv/LoadDiv.vue';
import StepHeader from 'pages/shared/private/session/parts/StepHeader.vue';
import StepFooter from 'pages/shared/private/session/parts/StepFooter.vue';
import { SessionStore } from 'pages/shared/private/session/utils/SessionStore';
import DrawerMenu from 'pages/shared/private/session/parts/DrawerMenu.vue';
import { sessionService } from 'src/common/services/session/session-service';

const route = useRoute();
const router = useRouter();
const rightDrawer = ref(false);
const loading = ref<boolean>(false);
const error = ref<boolean>(false);
const patient = ref<Patient>();
const sensorList = ref<MessageSensorListDto>();
const sessionStore = ref(new SessionStore());
const step = computed(() => sessionStore.value.actualStep);

const measurements = ref<Map<string, Set<SessionMeasurementDto>>>(new Map());

onUnmounted(() => {
  socket.disconnect();
});

const uuid = computed(() => route.params?.['uuid']?.toString());
onMounted(async () => {
  if (!uuid.value) {
    notify.error('Obrigatório o identificador da sessão');
    return await router.push({ name: 'private.session' });
  }

  try {
    loading.value = true;
    const { data } = await patientService.get({ uuid: uuid.value });
    const { data: metadata } = await sessionService.metadata();

    if (data.content) {
      patient.value = data.content;
    }

    if (metadata.content?.procedureTypes) {
      sessionStore.value.procedureTypes = metadata.content.procedureTypes;
    }

    socket.on('connect', () => {
      console.log('Conectado!', socket.id);
      socket.on(SocketEvents.LEAVE_ROOM, (data: string) => {
        console.log('welcome', data);
      });
      socket.on(SocketEvents.WELCOME, (data: string) => {
        console.log('welcome', data);
      });
      socket.on(SocketEvents.SERVER_CLIENT_SENSOR_LIST, (data: object) => {
        console.log(SocketEvents.SERVER_CLIENT_SENSOR_LIST, data);
        sensorList.value = plainToInstance(MessageSensorListDto, data);
      });
      socket.on(SocketEvents.SERVER_CLIENT_MEASUREMENT, (data: SessionMeasurementDto) => {
        console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data, measurements);
      });
    });
    socket.connect();
  } catch (e: unknown) {
    error.value = true;
    loading.value = false;
    notify.error(`Erro inesperado ao iniciar sessão ${(e as Error).message}`);
    return await router.push({ name: 'private.session' });
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <custom-page v-if="sessionStore">
    <error-div v-if="error" />
    <load-div v-else-if="loading" />
    <div v-else class="session-content">
      <step-header class="step-header" :session="sessionStore" :right-drawer="rightDrawer" />

      <q-card bordered flat class="step-content u-p-6">
        <Component :is="step.value" :session="sessionStore" />
        <drawer-menu :session="sessionStore" :right-drawer="rightDrawer" />
      </q-card>

      <step-footer class="step-footer" :session="sessionStore" />
    </div>
  </custom-page>
</template>

<style scoped lang="scss">
.session-content {
  height: 100%;
  width: 100%;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;

  .step-header {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }

  .step-content {
    flex: 1 1 0;
    height: 100%;
    width: 100%;
    min-height: 0;
  }

  .step-footer {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }
}
</style>
