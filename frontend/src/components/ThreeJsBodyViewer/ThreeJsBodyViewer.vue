<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import JointControl from './JointControl.vue';
import { useMannequin } from 'src/composables/use-manequin';

// Definições do UI
const jointDefs = [
  { name: 'head', label: 'Cabeça' },
  { name: 'neck', label: 'Pescoço' },
  { name: 'spine', label: 'Coluna' },
  { name: 'hip', label: 'Quadril' },
  { name: 'left_shoulder', label: 'Ombro Esq' },
  { name: 'left_elbow', label: 'Cotovelo Esq' },
  { name: 'right_shoulder', label: 'Ombro Dir' },
  { name: 'right_elbow', label: 'Cotovelo Dir' },
  { name: 'left_hip', label: 'Quadril Esq' },
  { name: 'left_knee', label: 'Joelho Esq' },
  { name: 'left_ankle', label: 'Tornozelo Esq' },
  { name: 'right_hip', label: 'Quadril Dir' },
  { name: 'right_knee', label: 'Joelho Dir' },
  { name: 'right_ankle', label: 'Tornozelo Dir' },
];

const sensorJointMap: Record<string, string> = {
  left_shin: 'left_knee',
  right_shin: 'right_knee',
  left_thigh: 'left_hip',
  right_thigh: 'right_hip',
  left_forearm: 'left_elbow',
  right_forearm: 'right_elbow',
  left_upper_arm: 'left_shoulder',
  right_upper_arm: 'right_shoulder',
  torso: 'spine',
  head: 'head',
  pelvis: 'hip',
};

// Refs & Estado
const canvasContainer = ref<HTMLElement | null>(null);
const jsonInput = ref('');
const inputError = ref(false);

const mannequin = useMannequin();

// Estado reativo das rotações
const jointRotations = reactive<Record<string, { x: number; y: number; z: number }>>({});
jointDefs.forEach((d) => {
  jointRotations[d.name] = { x: 0, y: 0, z: 0 };
});

// Playback
const playbackData = ref<any[]>([]);
const isPlaying = ref(false);
const currentFrame = ref(0);
let playInterval: number | null = null;

onMounted(() => {
  if (canvasContainer.value) {
    mannequin.init(canvasContainer.value);
    window.addEventListener('resize', handleResize);
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  mannequin.destroy();
  if (playInterval) clearInterval(playInterval);
});

const handleResize = () => {
  if (canvasContainer.value) {
    mannequin.resize(canvasContainer.value.clientWidth, canvasContainer.value.clientHeight);
  }
};

// Sincroniza slider com o modelo
const handleJointUpdate = (name: string, axis: 'x' | 'y' | 'z', value: number) => {
  jointRotations[name][axis] = value;
  mannequin.setJointRotation(
    name,
    jointRotations[name].x,
    jointRotations[name].y,
    jointRotations[name].z,
  );
};

// Lógica de Dados
const loadData = () => {
  try {
    const raw = JSON.parse(jsonInput.value);
    if (Array.isArray(raw) && raw.length > 0) {
      if (raw[0].readOrder !== undefined) {
        // Agrupa por readOrder
        const grouped: Record<string, any[]> = {};
        raw.forEach((m) => {
          if (!grouped[m.readOrder]) grouped[m.readOrder] = [];
          grouped[m.readOrder].push(m);
        });
        playbackData.value = Object.keys(grouped)
          .sort((a, b) => parseInt(a) - parseInt(b))
          .map((k) => grouped[k]);
      } else {
        playbackData.value = raw;
      }
      resetPlayback();
    }
  } catch (e) {
    inputError.value = true;
    setTimeout(() => {
      inputError.value = false;
    }, 1500);
  }
};

const applyMeasurement = (m: any) => {
  const jointName = sensorJointMap[m.sensorName] || m.sensorName;
  if (!jointRotations[jointName]) return;

  const rx = parseFloat(m.roll || m.eulerX || 0);
  const ry = parseFloat(m.pitch || m.eulerY || 0);
  const rz = parseFloat(m.yaw || m.eulerZ || 0);

  handleJointUpdate(jointName, 'x', rx);
  handleJointUpdate(jointName, 'y', ry);
  handleJointUpdate(jointName, 'z', rz);
};

const applyFrame = (index: number) => {
  if (index < 0 || index >= playbackData.value.length) return;
  const frame = playbackData.value[index];

  if (Array.isArray(frame)) frame.forEach(applyMeasurement);
  else applyMeasurement(frame);

  currentFrame.value = index;
};

const togglePlayback = () => {
  if (isPlaying.value) {
    isPlaying.value = false;
    if (playInterval) clearInterval(playInterval);
  } else {
    if (playbackData.value.length === 0) return;
    isPlaying.value = true;
    playInterval = window.setInterval(() => {
      applyFrame(currentFrame.value);
      currentFrame.value = (currentFrame.value + 1) % playbackData.value.length;
    }, 60);
  }
};

const resetPlayback = () => {
  if (isPlaying.value) togglePlayback();
  currentFrame.value = 0;
  jointDefs.forEach((d) => {
    handleJointUpdate(d.name, 'x', 0);
    handleJointUpdate(d.name, 'y', 0);
    handleJointUpdate(d.name, 'z', 0);
  });
};
</script>

<template>
  <div class="viewer-layout">
    <div id="canvas-container" ref="canvasContainer">
      <div id="overlay-info">
        <span>■</span> LOW POLY MANNEQUIN<br />
        Arrastar para rotacionar · Scroll para zoom
      </div>
      <div class="floor-label">THREE.JS · VUE 3 COMPOSITION</div>
    </div>

    <div id="panel">
      <div class="panel-title">Articulações</div>
      <div class="panel-subtitle">Controle por eixo (graus) · -180° a 180°</div>

      <div class="joints-container">
        <JointControl
          v-for="def in jointDefs"
          :key="def.name"
          :name="def.name"
          :label="def.label"
          :rotation="jointRotations[def.name]"
          @update="handleJointUpdate"
        />
      </div>

      <div class="separator"></div>

      <div class="panel-title">Playback</div>
      <div class="panel-subtitle">Reproduzir array de Measurement</div>

      <div class="btn-row">
        <button class="btn" :class="{ active: isPlaying }" @click="togglePlayback">
          {{ isPlaying ? '⏸ Pause' : '▶ Play' }}
        </button>
        <button class="btn danger" @click="resetPlayback">↺ Reset</button>
      </div>

      <div class="playback-row">
        <input
          type="range"
          class="playback-slider"
          min="0"
          :max="Math.max(0, playbackData.length - 1)"
          v-model.number="currentFrame"
          @input="applyFrame(currentFrame)"
        />
        <span class="frame-label">
          {{ playbackData.length ? currentFrame + 1 : 0 }} / {{ playbackData.length }}
        </span>
      </div>

      <div class="separator"></div>

      <div class="panel-title">Dados</div>
      <div class="panel-subtitle">Cole JSON de Measurement[] aqui</div>
      <textarea
        class="data-input"
        :class="{ error: inputError }"
        v-model="jsonInput"
        placeholder='[{"roll":0,"pitch":0,"yaw":0,"sensorName":"left_shin", ...}]'
      ></textarea>
      <button class="btn" style="margin-top: 4px" @click="loadData">Carregar Dados</button>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600;700&display=swap');

/* Variáveis de estilo isoladas para o escopo do Viewer */
.viewer-layout {
  --bg: #0a0e17;
  --panel: #111827;
  --panel-border: #1e293b;
  --accent: #22d3ee;
  --accent2: #a78bfa;
  --accent3: #34d399;
  --text: #e2e8f0;
  --text-dim: #64748b;
  --danger: #f87171;

  background: var(--bg);
  color: var(--text);
  font-family: 'JetBrains Mono', monospace;
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
}

#canvas-container {
  flex: 1;
  position: relative;
}

#overlay-info {
  position: absolute;
  top: 16px;
  left: 16px;
  font-size: 11px;
  color: var(--text-dim);
  pointer-events: none;
  line-height: 1.6;
}
#overlay-info span {
  color: var(--accent);
}
.floor-label {
  position: absolute;
  bottom: 16px;
  left: 16px;
  font-size: 9px;
  color: var(--text-dim);
  pointer-events: none;
  letter-spacing: 1px;
  text-transform: uppercase;
}

#panel {
  width: 340px;
  background: var(--panel);
  border-left: 1px solid var(--panel-border);
  overflow-y: auto;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--panel-border);
    border-radius: 2px;
  }
}

.panel-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: var(--accent);
  margin-bottom: 4px;
}
.panel-subtitle {
  font-size: 10px;
  color: var(--text-dim);
  margin-bottom: 8px;
}

.joints-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-row {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.btn {
  flex: 1;
  padding: 8px 12px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  border: 1px solid var(--panel-border);
  border-radius: 6px;
  background: transparent;
  color: var(--text);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--accent);
    color: var(--accent);
  }
  &.active {
    background: rgba(34, 211, 238, 0.15);
    border-color: var(--accent);
    color: var(--accent);
  }
  &.danger:hover {
    border-color: var(--danger);
    color: var(--danger);
  }
}

.separator {
  height: 1px;
  background: var(--panel-border);
  margin: 4px 0;
}

.playback-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.playback-slider {
  flex: 1;
  -webkit-appearance: none;
  appearance: none;
  height: 4px;
  border-radius: 2px;
  background: var(--panel-border);
  outline: none;
  cursor: pointer;

  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: var(--accent);
    cursor: grab;
  }
}
.frame-label {
  font-size: 10px;
  color: var(--text-dim);
  min-width: 60px;
  text-align: right;
}

.data-input {
  width: 100%;
  height: 80px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--panel-border);
  border-radius: 6px;
  color: var(--text);
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
  padding: 8px;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;

  &:focus {
    border-color: var(--accent);
  }
  &.error {
    border-color: var(--danger);
  }
  &::placeholder {
    color: var(--text-dim);
  }
}
</style>
