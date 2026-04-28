<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useProvidedSessionEditor } from 'src/composables/use-session';
import { api } from 'boot/axios';
import { notify } from 'src/common/utils/NotifyUtils';
import type { SensorInfoRead } from 'src/common/api/generated/models';

const sessionEditor = useProvidedSessionEditor();

const bodySegments = ref([
  { name: 'head', label: 'Cabeça', cat: 'core', top: '12%', left: '50%' },
  { name: 'hip', label: 'Quadril', cat: 'core', top: '48%', left: '50%' },
  { name: 'left_shoulder', label: 'Ombro Esq', cat: 'upper', top: '25%', left: '37%' },
  { name: 'right_shoulder', label: 'Ombro Dir', cat: 'upper', top: '25%', left: '63%' },
  { name: 'left_arm', label: 'Braço Esq', cat: 'upper', top: '32%', left: '34%' },
  { name: 'right_arm', label: 'Braço Dir', cat: 'upper', top: '32%', left: '66%' },
  { name: 'left_forearm', label: 'Antebraço Esq', cat: 'upper', top: '43%', left: '31%' },
  { name: 'right_forearm', label: 'Antebraço Dir', cat: 'upper', top: '43%', left: '69%' },
  { name: 'left_hand', label: 'Mão Esq', cat: 'upper', top: '52%', left: '28%' },
  { name: 'right_hand', label: 'Mão Dir', cat: 'upper', top: '52%', left: '72%' },
  { name: 'left_thigh', label: 'Coxa Esq', cat: 'lower', top: '60%', left: '45%' },
  { name: 'right_thigh', label: 'Coxa Dir', cat: 'lower', top: '60%', left: '55%' },
  { name: 'left_shin', label: 'Canela Esq', cat: 'lower', top: '78%', left: '45%' },
  { name: 'right_shin', label: 'Canela Dir', cat: 'lower', top: '78%', left: '55%' },
  { name: 'left_foot', label: 'Pé Esq', cat: 'lower', top: '90%', left: '45%' },
  { name: 'right_foot', label: 'Pé Dir', cat: 'lower', top: '90%', left: '55%' },
]);

// Mapeamento das regras para formar Nós/Articulações baseados nos segmentos
const nodeDefinitions = [
  { id: 'knee_left', region: 'KNEE_LEFT', side: 'LEFT', segments: ['left_thigh', 'left_shin'] },
  {
    id: 'knee_right',
    region: 'KNEE_RIGHT',
    side: 'RIGHT',
    segments: ['right_thigh', 'right_shin'],
  },
  { id: 'ankle_left', region: 'ANKLE_LEFT', side: 'LEFT', segments: ['left_shin', 'left_foot'] },
  {
    id: 'ankle_right',
    region: 'ANKLE_RIGHT',
    side: 'RIGHT',
    segments: ['right_shin', 'right_foot'],
  },
  { id: 'hip_left', region: 'HIP_LEFT', side: 'LEFT', segments: ['hip', 'left_thigh'] },
  { id: 'hip_right', region: 'HIP_RIGHT', side: 'RIGHT', segments: ['hip', 'right_thigh'] },
  { id: 'elbow_left', region: 'ELBOW_LEFT', side: 'LEFT', segments: ['left_arm', 'left_forearm'] },
  {
    id: 'elbow_right',
    region: 'ELBOW_RIGHT',
    side: 'RIGHT',
    segments: ['right_arm', 'right_forearm'],
  },
  { id: 'wrist_left', region: 'WRIST_LEFT', side: 'LEFT', segments: ['left_forearm', 'left_hand'] },
  {
    id: 'wrist_right',
    region: 'WRIST_RIGHT',
    side: 'RIGHT',
    segments: ['right_forearm', 'right_hand'],
  },
];

const selectedSegmentName = ref<string | null>(null);
const selectedSensorMac = ref<string | null>(null);

const assignedSensors = ref<Record<string, string>>({});
const availableSensorsList = ref<SensorInfoRead[]>([]);
const loadingSensors = ref(false);

const selectedSegment = computed(() =>
  bodySegments.value.find((s) => s.name === selectedSegmentName.value),
);

const sensorOptions = computed(() => {
  return availableSensorsList.value.map((sensor) => ({
    label: `${sensor.sensorName || 'Sensor'} (${sensor.macAddress})`,
    value: sensor.macAddress,
  }));
});

const formedNodes = computed(() => {
  return nodeDefinitions
    .filter((nodeDef) => nodeDef.segments.every((seg) => assignedSensors.value[seg]))
    .map((nodeDef) => ({
      ...nodeDef,
      sensorMacs: nodeDef.segments.map((seg) => assignedSensors.value[seg]),
    }));
});

onMounted(() => {
  void fetchAvailableSensors();
});

async function fetchAvailableSensors() {
  loadingSensors.value = true;
  try {
    const { data } = await api.getApiSessionsSensorsAvailable({ page: 1, rpp: 50 });
    if (data.content && Array.isArray(data.content)) {
      availableSensorsList.value = data.content.list || [];
    }
  } catch (error: unknown) {
    console.log('Não foi possível carregar os sensores disponíveis.', error);
    notify.error('Não foi possível carregar os sensores disponíveis.');
  } finally {
    loadingSensors.value = false;
  }
}

function selectSegment(segmentName: string) {
  selectedSegmentName.value = segmentName;
  selectedSensorMac.value = assignedSensors.value[segmentName] || null;
}

async function assignSensorToSegment() {
  if (!selectedSegmentName.value || !selectedSensorMac.value) return;

  try {
    const targetMac = selectedSensorMac.value;

    // Verifica se o sensor já foi vinculado a ALGUM outro segmento nesta tela
    const isAlreadyUsedLocally = Object.values(assignedSensors.value).includes(targetMac);

    // Se NÃO foi vinculado ainda, chamamos a API. Se já foi, ignoramos a API pois ele já está na sessão.
    if (!isAlreadyUsedLocally) {
      const sensorInfo = availableSensorsList.value.find((s) => s.macAddress === targetMac);
      if (sensorInfo && sessionEditor.addSensor) {
        await sessionEditor.addSensor(sensorInfo);
      }
    }

    assignedSensors.value[selectedSegmentName.value] = targetMac;
    notify.success(`Sensor vinculado à ${selectedSegment.value?.label} com sucesso!`);

    selectedSegmentName.value = null;
    selectedSensorMac.value = null;
  } catch (error: unknown) {
    console.error('Erro ao vincular sensor:', error);
    notify.error('Falha ao vincular o sensor ao segmento.');
  }
}

async function removeAssignment(segmentName: string) {
  const mac = assignedSensors.value[segmentName];
  const sessionId = sessionEditor.session.value.id;

  if (!sessionId || !mac) {
    return;
  }
  // Verifica se o MAC ainda está sendo utilizado em algum outro segmento
  const stillInUse = Object.values(assignedSensors.value).includes(mac);

  // Se não estiver em uso em NENHUM lugar, podemos remover da API de fato
  if (!stillInUse) {
    try {
      // Como a API removeSensor não retorna nada complexo, podemos só aguardar
      await api.deleteApiSessionsSessionIdSensorsMacAddress(sessionId, mac);

      delete assignedSensors.value[segmentName];
    } catch (e) {
      console.error('Erro ao remover sensor da sessão', e);
    }
  }
}
</script>

<template>
  <div class="first-step-container row q-col-gutter-xl q-pa-md">
    <div class="col-12 col-md-5 flex flex-center">
      <div class="body-container">
        <div class="body-silhouette">
          <img src="/body.png" alt="Mapa Anatômico" class="silhouette-img" />
        </div>

        <button
          v-for="segment in bodySegments"
          :key="segment.name"
          class="sensor-point"
          :class="{
            'is-selected': selectedSegmentName === segment.name,
            'is-assigned': assignedSensors[segment.name] && selectedSegmentName !== segment.name,
          }"
          :style="{ top: segment.top, left: segment.left }"
          @click="selectSegment(segment.name)"
        >
          <q-tooltip anchor="top middle" self="bottom middle" :offset="[10, 10]">
            {{ segment.label }}
            <div v-if="assignedSensors[segment.name]" class="text-caption text-green-3">
              Vinculado: {{ assignedSensors[segment.name] }}
            </div>
          </q-tooltip>
        </button>
      </div>
    </div>

    <div class="col-12 col-md-7 column">
      <h2 class="text-h5 q-mb-md">Configuração de Nós e Sensores</h2>
      <p class="text-body2 text-grey-7 q-mb-lg">
        Selecione um ponto no mapa do corpo humano ao lado para vincular um giroscópio. A combinação
        de segmentos com sensores ativados formará os nós (ex: Canela + Coxa = Joelho).
      </p>

      <q-card v-if="selectedSegment" bordered flat class="bg-grey-1 q-mb-md">
        <q-card-section>
          <div class="text-subtitle1 text-weight-bold q-mb-sm">
            Segmento Selecionado: <span class="text-primary">{{ selectedSegment.label }}</span>
          </div>

          <div class="row q-col-gutter-sm items-center">
            <div class="col-12 col-sm-8">
              <q-select
                v-model="selectedSensorMac"
                :options="sensorOptions"
                :loading="loadingSensors"
                label="Selecione o Sensor (MAC)"
                outlined
                dense
                emit-value
                map-options
                clearable
              >
                <template v-slot:no-option>
                  <q-item>
                    <q-item-section class="text-grey"> Nenhum sensor disponível </q-item-section>
                  </q-item>
                </template>
              </q-select>
            </div>
            <div class="col-12 col-sm-4">
              <q-btn
                color="primary"
                label="Vincular"
                class="full-width"
                :disable="!selectedSensorMac"
                @click="assignSensorToSegment"
              />
            </div>
          </div>
        </q-card-section>
      </q-card>

      <q-card
        v-else
        bordered
        flat
        class="bg-grey-1 q-mb-md flex flex-center"
        style="min-height: 120px"
      >
        <div class="text-grey-6 text-center">
          <q-icon name="touch_app" size="2em" class="q-mb-sm block center-block" />
          Clique em um ponto do corpo para configurar
        </div>
      </q-card>

      <div class="scroll-area flex-grow-1" style="max-height: 400px; overflow-y: auto">
        <div class="text-subtitle2 q-mb-sm">Sensores e Segmentos:</div>
        <q-list bordered separator class="rounded-borders q-mb-md">
          <q-item v-if="Object.keys(assignedSensors).length === 0">
            <q-item-section class="text-grey-6">Nenhum sensor vinculado ainda.</q-item-section>
          </q-item>

          <q-item v-for="(mac, segName) in assignedSensors" :key="segName">
            <q-item-section avatar>
              <q-icon name="sensors" color="positive" />
            </q-item-section>

            <q-item-section>
              <q-item-label>{{ bodySegments.find((s) => s.name === segName)?.label }}</q-item-label>
              <q-item-label caption>MAC: {{ mac }}</q-item-label>
            </q-item-section>

            <q-item-section side>
              <q-btn
                flat
                round
                color="negative"
                icon="delete"
                size="sm"
                @click="removeAssignment(segName)"
              >
                <q-tooltip>Remover vínculo</q-tooltip>
              </q-btn>
            </q-item-section>
          </q-item>
        </q-list>

        <div class="text-subtitle2 q-mb-sm">Nós Formados (Articulações):</div>
        <q-list bordered separator class="rounded-borders">
          <q-item v-if="formedNodes.length === 0">
            <q-item-section class="text-grey-6"
              >Nenhum nó formado ainda. Combine segmentos adjacentes.</q-item-section
            >
          </q-item>

          <q-item v-for="node in formedNodes" :key="node.id" class="bg-indigo-1">
            <q-item-section avatar>
              <q-icon name="hub" color="primary" />
            </q-item-section>

            <q-item-section>
              <q-item-label class="text-weight-medium">{{ node.region }}</q-item-label>
              <q-item-label caption>Sensores: {{ node.sensorMacs.join(' + ') }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </div>

      <div class="q-mt-auto text-right pt-4">
        <q-btn
          outline
          color="primary"
          icon="refresh"
          label="Recarregar Sensores"
          @click="fetchAvailableSensors"
          class="q-mt-md"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.first-step-container {
  height: 100%;
  width: 100%;
}

.body-container {
  position: relative;
  width: 320px;
  height: 550px;
  background-color: #fff;
  border: 2px solid $grey-3;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.body-silhouette {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  padding: 10px;
}

.silhouette-img {
  max-height: 100%;
  max-width: 100%;
  object-fit: contain;
}

.sensor-point {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background-color: rgba($grey-7, 0.7);
  border: 2px solid white;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  padding: 0;
}

.sensor-point:hover {
  transform: translate(-50%, -50%) scale(1.2);
  background-color: $primary;
  opacity: 1;
}

.sensor-point.is-selected {
  background-color: $primary;
  border-color: white;
  transform: translate(-50%, -50%) scale(1.3);
  box-shadow: 0 0 10px rgba($primary, 0.6);
  z-index: 11;
  opacity: 1;
}

.sensor-point.is-assigned {
  background-color: $positive;
  border-color: white;
  opacity: 1;
}
</style>
