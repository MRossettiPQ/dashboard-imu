<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { socket, socketEvents } from 'boot/socket';
import { SensorListDto } from 'src/common/models/socket/SensorListDto';
import { plainToInstance } from 'class-transformer';
import { useRoute, useRouter } from 'vue-router';
import { patientService } from 'src/common/services/patient/patient-service';
import { notify } from 'src/common/utils/NotifyUtils';
import type { Patient } from 'src/common/models/patient/Patient';
import type { SessionMeasurementDto } from 'src/common/models/socket/SessionMeasurementDto';
import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';

const route = useRoute();
const router = useRouter();
const patient = ref<Patient>();
const sensorList = ref<SensorListDto>();

const measurements = ref<Map<string, Set<SessionMeasurementDto>>>(new Map());

onUnmounted(() => {
  socket.disconnect();
});

function listenMeasurement(sensor: SessionSensorDto) {
  socket.on(`${socketEvents.MEASUREMENTS}_${sensor.ip}`, (data: SessionMeasurementDto) => {
    console.log(data);
  });
}

const uuid = computed(() => route.params?.['uuid']?.toString());
onMounted(async () => {
  if (!uuid.value) {
    notify.error('Obrigatório o identificador da sessão');
    return await router.push({ name: 'private.session' });
  }

  try {
    const { data } = await patientService.get({ uuid: uuid.value });

    if (data.content) {
      patient.value = data.content;
    }
    socket.on('connect', () => {
      console.log('Conectado!', socket.id);
      socket.on(socketEvents.LEAVE_ROOM, (data: string) => {
        console.log('welcome', data);
      });
      socket.on(socketEvents.WELCOME, (data: string) => {
        console.log('welcome', data);
      });
      socket.on(socketEvents.SENSOR_LIST, (data: object) => {
        sensorList.value = plainToInstance(SensorListDto, data);
      });
    });
    socket.connect();
  } catch (e: unknown) {
    notify.error(`Erro inesperado ao iniciar sessão ${(e as Error).message}`);
    return await router.push({ name: 'private.session' });
  }
});
</script>

<template>
  <custom-page>
    <div class="u-w-100 u-h-100 flex u-gap-18 u-pb-12">
      <div>Sessão</div>
      <div>{{ measurements }}</div>
    </div>
  </custom-page>
</template>
