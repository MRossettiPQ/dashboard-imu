<script setup lang="ts">
import { ref, computed } from 'vue';
import { SessionSensor, SessionState } from 'src/common/api/manual/constructors_api';
import type { SessionNodeCreateOrUpdate } from 'src/common/api/generated/models';
import { BodySideEnum } from 'src/common/api/generated/models';

interface Props {
  session: SessionState;
}

const props = defineProps<Props>();

// ─── Body joint definitions ────────────────────────────────────────────────

interface BodyPoint {
  key: string;
  label: string;
  side: BodySideEnum;
  cx: number;
  cy: number;
}

const bodyPoints: BodyPoint[] = [
  { key: 'head',           label: 'Cabeça',          side: BodySideEnum.CENTRAL, cx: 100, cy: 32  },
  { key: 'chest',          label: 'Tronco',           side: BodySideEnum.CENTRAL, cx: 100, cy: 132 },
  { key: 'hip',            label: 'Quadril',          side: BodySideEnum.CENTRAL, cx: 100, cy: 212 },
  { key: 'left_shoulder',  label: 'Ombro Esq.',       side: BodySideEnum.LEFT,    cx: 57,  cy: 80  },
  { key: 'right_shoulder', label: 'Ombro Dir.',       side: BodySideEnum.RIGHT,   cx: 143, cy: 80  },
  { key: 'left_elbow',     label: 'Cotovelo Esq.',    side: BodySideEnum.LEFT,    cx: 37,  cy: 165 },
  { key: 'right_elbow',    label: 'Cotovelo Dir.',    side: BodySideEnum.RIGHT,   cx: 163, cy: 165 },
  { key: 'left_wrist',     label: 'Pulso Esq.',       side: BodySideEnum.LEFT,    cx: 28,  cy: 240 },
  { key: 'right_wrist',    label: 'Pulso Dir.',       side: BodySideEnum.RIGHT,   cx: 172, cy: 240 },
  { key: 'left_knee',      label: 'Joelho Esq.',      side: BodySideEnum.LEFT,    cx: 79,  cy: 338 },
  { key: 'right_knee',     label: 'Joelho Dir.',      side: BodySideEnum.RIGHT,   cx: 121, cy: 338 },
  { key: 'left_ankle',     label: 'Tornozelo Esq.',   side: BodySideEnum.LEFT,    cx: 75,  cy: 428 },
  { key: 'right_ankle',    label: 'Tornozelo Dir.',   side: BodySideEnum.RIGHT,   cx: 125, cy: 428 },
];

// ─── Mock sensors ──────────────────────────────────────────────────────────
// TODO: substituir por session.sessionSensors quando sensores conectarem via MQTT
const mockSensors = ref<SessionSensor[]>(
  ['IMU-01', 'IMU-02', 'IMU-03', 'IMU-04', 'IMU-05'].map((name, i) => {
    const s = new SessionSensor();
    s.id = i + 1;
    s.observation = name;
    return s;
  }),
);

const availableSensors = computed<SessionSensor[]>(() => {
  const real = props.session.sessionSensors;
  return real.length > 0 ? real : mockSensors.value;
});

// ─── Dialog state ──────────────────────────────────────────────────────────

const dialogOpen = ref(false);
const selectedPoint = ref<BodyPoint | null>(null);

function openDialog(point: BodyPoint) {
  selectedPoint.value = point;
  dialogOpen.value = true;
}

// ─── Node helpers ──────────────────────────────────────────────────────────

function getNodeForKey(key: string): SessionNodeCreateOrUpdate | undefined {
  return props.session.sessionNodes.find((n) => n.observation === key);
}

function getAssignedSensor(key: string): SessionSensor | undefined {
  const node = getNodeForKey(key);
  if (!node?.nodeSensors?.length) return undefined;
  const sid = node.nodeSensors[0]?.sessionSensorId;
  return availableSensors.value.find((s) => s.id === sid);
}

function isAssigned(key: string): boolean {
  return !!getNodeForKey(key);
}

function sensorAlreadyUsed(sensor: SessionSensor): boolean {
  return props.session.sessionNodes.some((n) =>
    n.nodeSensors?.some((ns) => ns.sessionSensorId === sensor.id),
  );
}

function assignSensor(sensor: SessionSensor) {
  if (!selectedPoint.value) return;
  const { key, side } = selectedPoint.value;
  const existing = props.session.sessionNodes;
  const idx = existing.findIndex((n) => n.observation === key);

  const newNode: SessionNodeCreateOrUpdate = {
    observation: key,
    side,
    nodeSensors: [{ sessionSensorId: sensor.id ?? null }],
  };

  if (idx >= 0) {
    const updated = [...existing];
    updated[idx] = newNode;
    props.session.sessionNodes = updated;
  } else {
    props.session.sessionNodes = [...existing, newNode];
  }
  dialogOpen.value = false;
}

function removeAssignment(key: string) {
  props.session.sessionNodes = props.session.sessionNodes.filter(
    (n) => n.observation !== key,
  );
}

const configuredNodes = computed(() => bodyPoints.filter((p) => isAssigned(p.key)));

function getLabelForKey(key: string): string {
  return bodyPoints.find((p) => p.key === key)?.label ?? key;
}
</script>

<template>
  <div class="first-step-layout">

    <!-- ── Left: body selector ── -->
    <div class="body-panel">
      <p class="body-hint text-caption text-grey-6 q-mb-sm">
        Clique em uma articulação para atribuir um sensor
      </p>

      <div class="body-svg-wrapper">
        <svg
          viewBox="0 0 200 460"
          xmlns="http://www.w3.org/2000/svg"
          class="body-svg"
          aria-label="Seletor de articulações do corpo humano"
        >
          <!-- ── Silhouette ── -->
          <g class="silhouette" fill="#e8eaf0" stroke="#c5cae9" stroke-width="1.2">
            <!-- Head -->
            <circle cx="100" cy="32" r="26" />
            <!-- Neck -->
            <polygon points="91,57 109,57 112,74 88,74" />
            <!-- Torso -->
            <path d="M57,78 L143,78 L136,195 L64,195 Z" />
            <!-- Pelvis -->
            <path d="M64,195 L136,195 L139,228 L61,228 Z" />
            <!-- Left upper arm -->
            <path d="M50,80 L66,82 L45,168 L29,165 Z" />
            <!-- Right upper arm -->
            <path d="M134,82 L150,80 L171,165 L155,168 Z" />
            <!-- Left forearm -->
            <path d="M29,168 L45,170 L36,244 L20,241 Z" />
            <!-- Right forearm -->
            <path d="M155,170 L171,168 L180,241 L164,244 Z" />
            <!-- Left hand -->
            <ellipse cx="28" cy="256" rx="12" ry="16" />
            <!-- Right hand -->
            <ellipse cx="172" cy="256" rx="12" ry="16" />
            <!-- Left thigh -->
            <path d="M63,228 L97,228 L95,340 L61,340 Z" />
            <!-- Right thigh -->
            <path d="M103,228 L137,228 L139,340 L105,340 Z" />
            <!-- Left shin -->
            <path d="M62,342 L94,342 L92,430 L60,430 Z" />
            <!-- Right shin -->
            <path d="M106,342 L138,342 L140,430 L108,430 Z" />
            <!-- Left foot -->
            <path d="M52,430 L92,430 L92,448 L42,446 Z" />
            <!-- Right foot -->
            <path d="M108,430 L148,430 L158,446 L108,448 Z" />
          </g>

          <!-- ── Joint points ── -->
          <g v-for="point in bodyPoints" :key="point.key">
            <!-- Glow ring when assigned -->
            <circle
              v-if="isAssigned(point.key)"
              :cx="point.cx"
              :cy="point.cy"
              r="14"
              fill="rgba(99,179,110,0.18)"
              stroke="#63b36e"
              stroke-width="1.5"
            />

            <!-- Clickable joint circle -->
            <circle
              :cx="point.cx"
              :cy="point.cy"
              r="8"
              :fill="isAssigned(point.key) ? '#63b36e' : '#5c6bc0'"
              stroke="white"
              stroke-width="2"
              class="joint-point"
              :class="{ assigned: isAssigned(point.key) }"
              @click="openDialog(point)"
            >
              <title>{{ point.label }}</title>
            </circle>

            <!-- Sensor badge label -->
            <text
              v-if="isAssigned(point.key)"
              :x="point.cx"
              :y="point.cy - 13"
              text-anchor="middle"
              class="sensor-badge-text"
            >
              {{ getAssignedSensor(point.key)?.observation ?? '?' }}
            </text>
          </g>
        </svg>
      </div>
    </div>

    <!-- ── Right: configured nodes list ── -->
    <div class="nodes-panel">
      <div class="nodes-header">
        <span class="text-subtitle2 text-weight-medium">Articulações configuradas</span>
        <q-badge
          :label="configuredNodes.length"
          color="primary"
          class="q-ml-sm"
        />
      </div>

      <q-separator class="q-my-sm" />

      <div v-if="configuredNodes.length === 0" class="empty-state">
        <q-icon name="touch_app" size="32px" color="grey-4" />
        <p class="text-caption text-grey-5 q-mt-sm">
          Nenhuma articulação configurada ainda.
          <br />Clique nos pontos do corpo para atribuir sensores.
        </p>
      </div>

      <q-list v-else separator dense>
        <q-item
          v-for="point in configuredNodes"
          :key="point.key"
          class="node-item"
        >
          <q-item-section avatar>
            <q-avatar size="32px" color="green-1" text-color="green-8" icon="sensors" />
          </q-item-section>

          <q-item-section>
            <q-item-label class="text-weight-medium text-body2">
              {{ point.label }}
            </q-item-label>
            <q-item-label caption class="text-grey-6">
              {{ getAssignedSensor(point.key)?.observation ?? 'Sensor não identificado' }}
            </q-item-label>
          </q-item-section>

          <q-item-section side>
            <div class="row q-gutter-xs">
              <q-btn
                flat
                round
                dense
                size="sm"
                icon="edit"
                color="primary"
                @click="openDialog(point)"
              >
                <q-tooltip>Alterar sensor</q-tooltip>
              </q-btn>
              <q-btn
                flat
                round
                dense
                size="sm"
                icon="close"
                color="negative"
                @click="removeAssignment(point.key)"
              >
                <q-tooltip>Remover</q-tooltip>
              </q-btn>
            </div>
          </q-item-section>
        </q-item>
      </q-list>
    </div>

    <!-- ── Sensor assignment dialog ── -->
    <q-dialog v-model="dialogOpen" persistent>
      <q-card style="min-width: 320px; max-width: 420px">
        <q-card-section class="row items-center q-pb-none">
          <div>
            <p class="text-subtitle1 text-weight-medium q-mb-none">
              {{ selectedPoint?.label }}
            </p>
            <p class="text-caption text-grey-6 q-mb-none">
              Selecione o sensor que será colocado nesta articulação
            </p>
          </div>
          <q-space />
          <q-btn flat round dense icon="close" v-close-popup />
        </q-card-section>

        <q-separator class="q-mt-sm" />

        <q-card-section class="q-pt-sm">
          <q-list separator>
            <q-item
              v-for="sensor in availableSensors"
              :key="String(sensor.id)"
              clickable
              :disable="
                sensorAlreadyUsed(sensor) &&
                getAssignedSensor(selectedPoint?.key ?? '')?.id !== sensor.id
              "
              class="sensor-option"
              :class="{
                'sensor-option--active':
                  getAssignedSensor(selectedPoint?.key ?? '')?.id === sensor.id,
              }"
              @click="assignSensor(sensor)"
            >
              <q-item-section avatar>
                <q-avatar
                  size="36px"
                  :color="
                    getAssignedSensor(selectedPoint?.key ?? '')?.id === sensor.id
                      ? 'green-2'
                      : 'blue-grey-1'
                  "
                  :text-color="
                    getAssignedSensor(selectedPoint?.key ?? '')?.id === sensor.id
                      ? 'green-8'
                      : 'blue-grey-6'
                  "
                  icon="sensors"
                />
              </q-item-section>

              <q-item-section>
                <q-item-label class="text-weight-medium">
                  {{ sensor.observation ?? `Sensor ${sensor.id}` }}
                </q-item-label>
                <q-item-label caption class="text-grey-5">
                  {{
                    sensorAlreadyUsed(sensor) &&
                    getAssignedSensor(selectedPoint?.key ?? '')?.id !== sensor.id
                      ? 'Já em uso'
                      : 'Disponível'
                  }}
                </q-item-label>
              </q-item-section>

              <q-item-section side>
                <q-icon
                  v-if="getAssignedSensor(selectedPoint?.key ?? '')?.id === sensor.id"
                  name="check_circle"
                  color="green-6"
                  size="20px"
                />
              </q-item-section>
            </q-item>
          </q-list>

          <div v-if="availableSensors.length === 0" class="text-center q-py-md">
            <q-icon name="sensors_off" size="28px" color="grey-4" />
            <p class="text-caption text-grey-5 q-mt-xs">Nenhum sensor disponível</p>
          </div>
        </q-card-section>

        <q-card-actions align="right" class="q-pt-none">
          <q-btn flat label="Cancelar" color="grey-7" v-close-popup />
          <q-btn
            v-if="isAssigned(selectedPoint?.key ?? '')"
            flat
            label="Remover atribuição"
            color="negative"
            @click="removeAssignment(selectedPoint!.key); dialogOpen = false"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<style scoped lang="scss">
.first-step-layout {
  display: flex;
  flex-direction: row;
  gap: 24px;
  height: 100%;
  min-height: 0;
  min-width: 0;
  width: 100%;
  padding: 8px 4px;
  overflow: hidden;
}

// ── Body panel ──────────────────────────────────────────────────────────────

.body-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 240px;
}

.body-hint {
  text-align: center;
  line-height: 1.4;
}

.body-svg-wrapper {
  width: 200px;
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.body-svg {
  width: 100%;
  height: 100%;
  max-height: 460px;
  overflow: visible;
}

// ── Joint points ─────────────────────────────────────────────────────────────

.joint-point {
  cursor: pointer;
  transition: filter 0.15s ease;

  &:hover {
    filter: drop-shadow(0 0 4px rgba(92, 107, 192, 0.6));
  }

  &.assigned:hover {
    filter: drop-shadow(0 0 4px rgba(99, 179, 110, 0.7));
  }
}

.sensor-badge-text {
  font-size: 7px;
  fill: #388e3c;
  font-weight: 600;
  pointer-events: none;
  letter-spacing: 0.2px;
}

// ── Nodes panel ──────────────────────────────────────────────────────────────

.nodes-panel {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nodes-header {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #bbb;
  padding: 24px;
}

.node-item {
  border-radius: 6px;
  transition: background 0.15s;

  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }
}

// ── Dialog sensor list ───────────────────────────────────────────────────────

.sensor-option {
  border-radius: 8px;
  transition: background 0.12s;

  &--active {
    background: rgba(99, 179, 110, 0.1);
  }

  &.q-item--disable {
    opacity: 0.45;
  }
}
</style>
