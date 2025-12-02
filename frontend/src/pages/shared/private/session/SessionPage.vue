<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, markRaw, onMounted, onUnmounted, ref } from 'vue';
import { socket } from 'boot/socket';
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
import type { Sensor } from 'src/common/models/sensor/Sensor';
import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

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
const availableSensorList = ref<SessionSensorDto[]>([]);
const availableProcedureList = ref<ProcedureType[]>([]);

// Sensores aplicados a sessão
const selectedSensorList = ref<Set<Sensor>>(new Set());

// Tipo de visualização na tela de detalhes
const viewType = ref<'grid' | 'unified' | 'table' | 'summary'>('grid');

// Procedimento e movimento escolhidos para realização no momento
const selectedProcedure = ref<Procedure | undefined>();
const selectedMovement = ref<Movement | undefined>();

onUnmounted(() => {
  for (const sensor of Array.from(selectedSensorList.value)) {
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
      availableProcedureList.value = metadata.content.procedureTypes;
    }

    socket.on('connect', () => {
      console.log('Conectado!', socket.id);
    });
    socket.on(SocketEvents.WELCOME, (data: string) => {
      console.log('WELCOME', data);
    });
    socket.on(SocketEvents.LEAVE_ROOM, (data: string) => {
      console.log('LEAVE_ROOM', data);
    });
    socket.on(SocketEvents.SERVER_CLIENT_SENSOR_LIST, (data: object) => {
      const { content } = plainToInstance(MessageSensorListDto, data);
      availableSensorList.value = content ?? [];
    });
    socket.on(SocketEvents.SERVER_CLIENT_MEASUREMENT, (data: MessageClientMeasurementBlock) => {
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data);
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
        v-model:view-type="viewType"
        :selected-sensor-list="selectedSensorList"
        :actual-step-name="actualStepName"
        :actual-step-label="actualStepLabel"
        :session="session"
        :selected-movement="selectedMovement"
      />

      <q-card bordered flat class="step-content u-p-6">
        <Component
          :is="actualStepValue"
          v-model:right-drawer="rightDrawer"
          v-model:session="session"
          v-model:selected-movement="selectedMovement"
          v-model:selected-procedure="selectedProcedure"
          :selected-sensor-list="selectedSensorList"
          :available-sensor-list="availableSensorList"
          :available-procedure-list="availableProcedureList"
          :view-type="viewType"
          :in-progress="inProgress"
          :loading-save="loadingSave"
        />
        <drawer-menu
          v-model:session="session"
          v-model:right-drawer="rightDrawer"
          v-model:selected-movement="selectedMovement"
          v-model:selected-procedure="selectedProcedure"
          :selected-sensor-list="selectedSensorList"
        />
      </q-card>

      <step-footer
        class="step-footer"
        v-if="session"
        :session="session"
        @next="next"
        @prev="prev"
        :in-progress="inProgress"
        :loading-save="loadingSave"
        :actual-step-name="actualStepName"
        :selected-sensor-list="selectedSensorList"
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
