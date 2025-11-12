<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, markRaw, onMounted, onUnmounted, ref } from 'vue';
import { socket, SocketEvents } from 'boot/socket';
import { MessageSensorListDto } from 'src/common/models/socket/MessageSensorListDto';
import { plainToInstance } from 'class-transformer';
import { useRoute, useRouter } from 'vue-router';
import { patientService } from 'src/common/services/patient/patient-service';
import { notify } from 'src/common/utils/NotifyUtils';
import type { SessionMeasurementDto } from 'src/common/models/socket/SessionMeasurementDto';
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
import type { ProcedureType } from 'src/common/models/procedure/Procedure';
import { Procedure } from 'src/common/models/procedure/Procedure';
import { Movement, type MovementType } from 'src/common/models/movement/Movement';
import { PositionEnum, Sensor, SensorType } from 'src/common/models/sensor/Sensor';
import dayjs from 'dayjs';
import { Measurement } from 'src/common/models/measurement/Measurement';
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
const rightDrawer = ref(false);
const loading = ref<boolean>(false);
const error = ref<boolean>(false);
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
  // Verificação de segurança
  if (!steps.value || steps.value.length === 0) {
    console.error('Steps não inicializados');
    return;
  }

  if (actualStepOrder.value < steps.value.length - 1) {
    nextStep();
  } else {
    console.log('Não há próximo passo disponível');
  }
}

function nextStep() {
  if (!steps.value) return;

  const nextStep = steps.value.find(({ order }) => order === selectedStep.value.order + 1);
  if (nextStep) {
    selectedStep.value = nextStep;
  }
}

function prev(): void {
  if (!steps.value) return;

  const prevStep = steps.value.find(({ order }) => order === selectedStep.value.order - 1);
  if (prevStep) {
    selectedStep.value = prevStep;
  }
}

const sensorList = ref<SessionSensorDto[]>([]);
const measurements = ref<Map<string, Set<SessionMeasurementDto>>>(new Map());
const patient = ref<Patient>();
const selectedSensors = ref<Set<Sensor>>(new Set());
const session = ref<Session>(new Session());
const procedureTypes = ref<ProcedureType[]>([]);
const inProgress = ref(false);
const loadingSave = ref(false);
const requestedSensorList = ref(false);
const selectedProcedure = ref<Procedure | undefined>();
const selectedMovement = ref<Movement | undefined>();
const viewType = ref<'grid' | 'unified' | 'table' | 'summary'>('grid');
const actualMovementLabel = computed(() => selectedMovement.value?.type);

async function addSensorListener(sessionSensorDto: SessionSensorDto) {
  console.log('addSensorListener', sessionSensorDto);
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
    socket.on(event, (data: SessionMeasurementDto) => {
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data, measurements.value);
    });
  }
}

async function commandCalibrate(sessionSensorDto: SessionSensorDto) {
  const message = new MessageCalibrateSensorDto();
  const content = new CalibrateSensorDto();
  content.sensor = sessionSensorDto.clientId!;
  message.content = content;
  await socket.emitWithAck(SocketEvents.CLIENT_SERVER_CALIBRATE, message);
}

async function removeSensorListener(sessionSensorDto: SessionSensorDto) {
  if (!sessionSensorDto.mac) return;

  const sensors = Array.from(selectedSensors.value);
  const alreadyExists = sensors.some((s) => s.macAddress === sessionSensorDto.mac);
  if (!alreadyExists) return;

  const removeSensor = new MessageRemoveSensorDto();
  const content = new RemoveSensorDto();
  content.sensor = sessionSensorDto.clientId ?? '';
  removeSensor.content = content;
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_REMOVE_SENSOR, removeSensor);
  if (r == 'REMOVED_ROOM') {
    const event = `${SocketEvents.SERVER_CLIENT_MEASUREMENT}:${sessionSensorDto.mac}`;
    socket.removeListener(event);
  }
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

function selectProcedure(index: number): void {
  selectedProcedure.value = session.value.procedures[index];
  if (selectedProcedure.value) {
    selectedMovement.value = selectedProcedure.value.movements[0];
  }
}

function selectMovement(movement: Movement): void {
  console.log(movement);
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

function addProcedure(
  procedureType: ProcedureType | undefined,
  movementTypes: MovementType[],
): void {
  if (!procedureType?.type) {
    return;
  }

  let procedure = session.value.procedures.find((p) => p.type === procedureType.type);

  if (!procedure) {
    procedure = new Procedure();
  }

  procedure.type = procedureType.type;

  for (const movementType of movementTypes) {
    if (!movementType.type) {
      continue;
    }

    const movement = new Movement();
    movement.type = movementType.type;
    procedure.movements.push(movement);
  }

  session.value.procedures.push(procedure);
}

function addSample(sensorN: number = 2): void {
  const getRandomArbitrary = ({ min, max }: { min: number; max: number }): number =>
    Math.random() * (max - min) + min;

  selectedProcedure.value = session.value.procedures.pop();
  selectedProcedure.value ??= new Procedure();
  console.log(selectedProcedure.value);

  if (!selectedMovement.value) {
    selectedMovement.value = selectedProcedure.value.movements.pop();
  }

  if (!selectedMovement.value) {
    const movement = new Movement();
    const addIterator = 0;
    while (addIterator < sensorN) {
      const sensor = new Sensor();
      sensor.ip = `192.168.16.10${addIterator}`;
      sensor.sensorName = `Sensor ${addIterator}`;
      sensor.observation = `Sensor ${addIterator} de exemplo`;
      sensor.type = SensorType.GYROSCOPE;
      sensor.position = addIterator === 0 ? PositionEnum.ONE : PositionEnum.TWO;
      movement.sensors.push(sensor);
    }
    selectedMovement.value = movement;
  } else {
    for (const selectedSensor of selectedSensors.value) {
      const sensor = new Sensor();
      sensor.ip = selectedSensor.ip ?? '';
      sensor.sensorName = selectedSensor.sensorName ?? '';
      sensor.observation = selectedSensor.observation ?? '';
      sensor.type = SensorType.GYROSCOPE;
      sensor.position = selectedSensor.position ?? PositionEnum.ONE;
      selectedMovement.value.sensors.push(sensor);
    }
  }

  console.log(selectedMovement.value);
  if (!selectedMovement.value) return;

  const now = dayjs();
  let iterator: number = 0;
  while (iterator < 365) {
    iterator++;
    for (const sensor of selectedMovement.value.sensors) {
      const index: number = selectedMovement.value.sensors.indexOf(sensor);
      const number = sensor.measurements.length ? sensor.measurements.length : 0;
      const measurement = new Measurement();
      measurement.sensorName = sensor.sensorName ?? `undefined_sensor ${index}`;
      measurement.readOrder = number;

      measurement.roll = getRandomArbitrary({ min: 90, max: 80 });
      measurement.pitch = getRandomArbitrary({ min: 90, max: 80 });
      measurement.yaw = getRandomArbitrary({ min: 90, max: 80 });

      measurement.eulerX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.eulerY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.eulerZ = getRandomArbitrary({ min: 90, max: 80 });

      measurement.gyroRadsX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.gyroRadsY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.gyroRadsZ = getRandomArbitrary({ min: 90, max: 80 });

      measurement.accelLinX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.accelLinY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.accelLinZ = getRandomArbitrary({ min: 90, max: 80 });

      measurement.accelMssX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.accelMssY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.accelMssZ = getRandomArbitrary({ min: 90, max: 80 });

      measurement.magBiasX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.magBiasY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.magBiasZ = getRandomArbitrary({ min: 90, max: 80 });

      measurement.quaternionX = getRandomArbitrary({ min: 90, max: 80 });
      measurement.quaternionY = getRandomArbitrary({ min: 90, max: 80 });
      measurement.quaternionZ = getRandomArbitrary({ min: 90, max: 80 });
      measurement.quaternionW = getRandomArbitrary({ min: 90, max: 80 });

      measurement.capturedAt = now.add(number, 'ms');

      sensor.measurements.push(measurement);
    }
  }
  console.log(selectedMovement.value);
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
    socket.on(SocketEvents.SERVER_CLIENT_MEASUREMENT, (data: SessionMeasurementDto) => {
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data, measurements.value);
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
        v-model:rightDrawer="rightDrawer"
        v-model:viewType="viewType"
        :add-sample="addSample"
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
          :right-drawer="rightDrawer"
          :session="session"
          :sensor-list="sensorList"
          :selected-sensors="selectedSensors"
          :add-sample="addSample"
          :add-procedure="addProcedure"
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
          :select-procedure="selectProcedure"
          :select-movement="selectMovement"
        />
        <drawer-menu :right-drawer="rightDrawer" :session="session" :add-sample="addSample" />
      </q-card>

      <step-footer
        class="step-footer"
        :next="next"
        :prev="prev"
        :session="session"
        :in-progress="inProgress"
        :loading-save="loadingSave"
        :actual-step-name="actualStepName"
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
  }

  .step-footer {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }
}
</style>
