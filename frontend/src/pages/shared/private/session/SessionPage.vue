<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, markRaw, onMounted, onUnmounted, ref } from 'vue';
import { socket, SocketEvents } from 'boot/socket';
import { MessageSensorListDto } from 'src/common/models/socket/MessageSensorListDto';
import { plainToInstance } from 'class-transformer';
import { useRoute, useRouter } from 'vue-router';
import { patientService } from 'src/common/services/patient/patient-service';
import { notify } from 'src/common/utils/NotifyUtils';
import type { MessageClientMeasurementBlock } from 'src/common/models/socket/MessageClientMeasurementBlock';
import ErrorDiv from 'components/ErrorDiv/ErrorDiv.vue';
import LoadDiv from 'components/LoadDiv/LoadDiv.vue';
import StepHeader from 'pages/shared/private/session/parts/StepHeader.vue';
import StepFooter from 'pages/shared/private/session/parts/StepFooter.vue';
import DrawerMenu from 'pages/shared/private/session/parts/DrawerMenu.vue';
import { sessionService } from 'src/common/services/session/session-service';
import FirstStep from 'pages/shared/private/session/steps/first-step/FirstStep.vue';
import SecondStep from 'pages/shared/private/session/steps/second-step/SecondStep.vue';
import ThirdStep from 'pages/shared/private/session/steps/third-step/ThirdStep.vue';
import type { Patient } from 'src/common/models/patient/Patient';
import { Session } from 'src/common/models/session/Session';
import type { Procedure, ProcedureType } from 'src/common/models/procedure/Procedure';
import type { Movement } from 'src/common/models/movement/Movement';
import { findMovementEnum } from 'src/common/models/movement/Movement';
import { Sensor } from 'src/common/models/sensor/Sensor';
import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';
import {
  MessageRemoveSensorDto,
  RemoveSensorDto,
} from 'src/common/models/socket/MessageRemoveSensorDto';
import { AddSensorDto, MessageAddSensorDto } from 'src/common/models/socket/MessageAddSensorDto';
import {
  CalibrateSensorDto,
  MessageCalibrateSensorDto,
} from 'src/common/models/socket/MessageCalibrateSensorDto';

interface NavigationStep {
  order: number;
  name: 'first-step' | 'second-step' | 'third-step' | 'save-step';
  label: string;
  value: unknown;
}
const route = useRoute();
const router = useRouter();

// Menu lateral direito
const rightDrawer = ref(true);
// Ao montar a tela
const loading = ref<boolean>(false);
const error = ref<boolean>(false);
// Medições em progresso
const inProgress = ref(false);
// Ao finalizar sessão
const loadingSave = ref(false);
// Lista de sensores requisitada
const requestedSensorList = ref(false);

// Movimentação de telas
const steps = ref<NavigationStep[]>([
  {
    order: 0,
    name: 'first-step',
    label: 'Selecionar procedimento',
    value: markRaw(FirstStep),
  },
  {
    order: 1,
    name: 'second-step',
    label: 'Ativar sensores',
    value: markRaw(SecondStep),
  },
  {
    order: 2,
    name: 'third-step',
    label: 'Captar medições',
    value: markRaw(ThirdStep),
  },
  {
    order: 3,
    name: 'save-step',
    label: 'Salvar',
    value: markRaw(ThirdStep),
  },
]);

const selectedStep = ref<NavigationStep>(steps.value[0]!);
const actualStepOrder = computed(() => selectedStep.value.order);
const actualStepName = computed(() => selectedStep.value.name);
const actualStepValue = computed(() => selectedStep.value.value);
const actualStepLabel = computed(() => selectedStep.value.label);

function next() {
  if (!steps.value || steps.value.length === 0) {
    console.error('Steps não inicializados');
    return;
  }

  if (actualStepOrder.value < steps.value.length - 1) {
    const nextStep = steps.value.find(({ order }) => order === selectedStep.value.order + 1);
    if (nextStep) {
      selectedStep.value = nextStep;
    }
  } else {
    console.log('Não há próximo passo disponível');
  }
}

function prev(): void {
  if (!steps.value) return;

  const prevStep = steps.value.find(({ order }) => order === selectedStep.value.order - 1);
  if (prevStep) {
    selectedStep.value = prevStep;
  }
}

// Dados do paciente e dados da sessão
const patient = ref<Patient>();
const session = ref<Session>(new Session());

// Lista de sensores conectados ao backend e lista de procedimentos disponiveis
const sensorList = ref<SessionSensorDto[]>([]);
const procedureTypes = ref<ProcedureType[]>([]);

// Sensores aplicados a sessão
const selectedSensors = ref<Set<Sensor>>(new Set());

// Tipo de visualização na tela de detalhes
const viewType = ref<'grid' | 'unified' | 'table' | 'summary'>('grid');

// Procedimento e movimento escolhidos para realização no momento
const selectedProcedure = ref<Procedure | undefined>();
const selectedMovement = ref<Movement | undefined>();
const actualMovementLabel = computed(() => {
  if (selectedMovement.value?.type) {
    return findMovementEnum(selectedMovement.value.type) ?? 'Não encontrado';
  }
  return '';
});
async function addSensorListener(sessionSensorDto: SessionSensorDto) {
  if (!sessionSensorDto.mac) return;

  const sensors = Array.from(selectedSensors.value);
  const alreadyExists = sensors.some((s) => s.macAddress === sessionSensorDto.mac);
  if (alreadyExists) return;

  const sensor = new Sensor();
  sensor.ip = sessionSensorDto.ip ?? '';
  sensor.macAddress = sessionSensorDto.mac ?? '';
  sensor.sensorName = sessionSensorDto.name ?? '';
  selectedSensors.value.add(sensor);

  const addSensor = new MessageAddSensorDto();
  const content = new AddSensorDto();
  content.sensor = sessionSensorDto.clientId ?? '';
  addSensor.content = content;
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_ADD_SENSOR, addSensor);
  if (r == 'JOINED_ROOM') {
    const event = `${SocketEvents.SERVER_CLIENT_MEASUREMENT}:${sessionSensorDto.mac}`;
    socket.on(event, (data: MessageClientMeasurementBlock) => {
      const block = data.content;
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data);
      const sensors = selectedMovement.value?.sensors ?? [];
      const sensorIndex = sensors.findIndex((s) => s.macAddress == sessionSensorDto.mac);
      if (sensorIndex != -1) {
        const sensor = selectedMovement.value?.sensors?.[sensorIndex];
        if (sensor) {
          selectedMovement.value!.sensors[sensorIndex]!.measurements =
            sensor.measurements.concat(block);
        }
      }
    });
  }
}

async function removeSensorListener(sessionSensorDto: SessionSensorDto) {
  if (!sessionSensorDto.mac) return;

  const sensors = Array.from(selectedSensors.value);
  const alreadyExists = sensors.some((s) => s.macAddress === sessionSensorDto.mac);
  const alreadyExistsIndex = sensors.findIndex((s) => s.macAddress === sessionSensorDto.mac);
  if (!alreadyExists) return;

  const removeSensor = new MessageRemoveSensorDto();
  const content = new RemoveSensorDto();
  content.sensor = sessionSensorDto.clientId ?? '';
  removeSensor.content = content;

  const sensorToRemove = sensors[alreadyExistsIndex];
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_REMOVE_SENSOR, removeSensor);
  if (r == 'REMOVED_ROOM' && sensorToRemove) {
    const event = `${SocketEvents.SERVER_CLIENT_MEASUREMENT}:${sessionSensorDto.mac}`;
    socket.removeListener(event);
    selectedSensors.value.delete(sensorToRemove);
  }
}

async function commandCalibrate(sessionSensorDto: SessionSensorDto) {
  const message = new MessageCalibrateSensorDto();
  const content = new CalibrateSensorDto();
  content.sensor = sessionSensorDto.clientId!;
  message.content = content;
  await socket.emitWithAck(SocketEvents.CLIENT_SERVER_CALIBRATE, message);
}

async function commandRestart() {
  console.log('commandRestart');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_RESTART, '');
  console.log(r);
}

async function commandStart() {
  console.log('commandStart');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_STOP, '');
  console.log(r);
}

async function commandStop() {
  console.log('commandStop');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_STOP, '');
  console.log(r);
}

async function requestSensorList(): Promise<void> {
  try {
    requestedSensorList.value = true;
    await socket.emitWithAck(SocketEvents.CLIENT_SERVER_SENSOR_LIST, '');
  } catch (e) {
    console.error(e);
  } finally {
    requestedSensorList.value = false;
  }
}

onUnmounted(() => {
  for (const sensor of Array.from(selectedSensors.value)) {
    console.log(sensor);
    // removeSensorListener(sensor);
  }

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

    if (data.content && metadata.content?.procedureTypes) {
      patient.value = data.content;
      procedureTypes.value = metadata.content.procedureTypes;
    }

    console.log(selectedSensors);
    socket.on(SocketEvents.WELCOME, (data: string) => {
      console.log('WELCOME', data);
    });
    socket.on(SocketEvents.LEAVE_ROOM, (data: string) => {
      console.log('LEAVE_ROOM', data);
    });
    socket.on(SocketEvents.SERVER_CLIENT_SENSOR_LIST, (data: object) => {
      const { content } = plainToInstance(MessageSensorListDto, data);
      sensorList.value = content;
      console.log(SocketEvents.SERVER_CLIENT_SENSOR_LIST, sensorList.value);
    });
    socket.on(SocketEvents.SERVER_CLIENT_MEASUREMENT, (data: MessageClientMeasurementBlock) => {
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data);
    });
    socket.on('connect', () => {
      console.log('Conectado!', socket.id);
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
  <custom-page>
    <error-div v-if="error" />
    <load-div v-else-if="loading" />
    <div v-else class="session-content">
      <step-header
        class="step-header"
        v-model:right-drawer="rightDrawer"
        v-model:viewType="viewType"
        :request-sensor-list="requestSensorList"
        :requested-sensor-list="requestedSensorList"
        :selected-sensors="selectedSensors"
        :sensor-list="sensorList"
        :actual-step-name="actualStepName"
        :actual-step-order="actualStepOrder"
        :actual-step-label="actualStepLabel"
        :actual-movement-label="actualMovementLabel"
        :session="session"
        :selected-movement="selectedMovement"
      />

      <q-card bordered flat class="step-content u-p-6">
        <Component
          :is="actualStepValue"
          v-model:right-drawer="rightDrawer"
          v-model:session="session"
          :sensor-list="sensorList"
          :selected-sensors="selectedSensors"
          :add-sensor-listener="addSensorListener"
          :remove-sensor-listener="removeSensorListener"
          :procedure-types="procedureTypes"
          :selected-movement="selectedMovement"
          :view-type="viewType"
          :in-progress="inProgress"
          :loading-save="loadingSave"
          :command-start="commandStart"
          :command-restart="commandRestart"
          :command-stop="commandStop"
          :command-calibrate="commandCalibrate"
        />
        <drawer-menu
          :selected-sensors="selectedSensors"
          v-model:session="session"
          v-model:right-drawer="rightDrawer"
          v-model:selected-movement="selectedMovement"
          v-model:selected-procedure="selectedProcedure"
        />
      </q-card>

      <step-footer
        class="step-footer"
        v-if="session"
        :next="next"
        :prev="prev"
        :session="session"
        :in-progress="inProgress"
        :loading-save="loadingSave"
        :actual-step-name="actualStepName"
        :selected-sensors="selectedSensors"
      />
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
    min-width: 0;
    display: flex;
    flex-direction: row;
  }

  .step-footer {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }
}
</style>
