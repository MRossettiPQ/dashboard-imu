<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  name: string;
  label: string;
  rotation: { x: number; y: number; z: number };
}>();

const emit = defineEmits<{
  (e: 'update', name: string, axis: 'x'|'y'|'z', value: number): void
}>();

const localRot = ref({ ...props.rotation });
const isActive = ref(false);
let timeoutId: number;

watch(() => props.rotation, (newVal) => {
  localRot.value = { ...newVal };
}, { deep: true });

const onInput = (axis: 'x'|'y'|'z', event: Event) => {
  const val = parseFloat((event.target as HTMLInputElement).value);
  localRot.value[axis] = val;
  emit('update', props.name, axis, val);

  // Efeito visual temporário
  isActive.value = true;
  clearTimeout(timeoutId);
  timeoutId = window.setTimeout(() => { isActive.value = false; }, 500);
};
</script>

<template>
  <div class="joint-group" :class="{ active: isActive }">
    <div class="joint-header">
      <span class="joint-name">{{ label }}</span>
      <span class="joint-badge">{{ name }}</span>
    </div>

    <div v-for="axis in (['x', 'y', 'z'] as const)" :key="axis" class="axis-row">
      <span class="axis-label" :class="axis">{{ axis.toUpperCase() }}</span>
      <input
        type="range"
        class="axis-slider" :class="axis"
        min="-180" max="180" step="0.1"
        :value="localRot[axis]"
        @input="onInput(axis, $event)"
      >
      <span class="axis-value">{{ localRot[axis].toFixed(1) }}°</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.joint-group {
  background: rgba(255,255,255,0.02);
  border: 1px solid var(--panel-border);
  border-radius: 8px;
  padding: 12px;
  transition: border-color 0.2s;

  &:hover { border-color: var(--accent); }
  &.active { border-color: var(--accent); box-shadow: 0 0 12px rgba(34,211,238,0.1); }
}

.joint-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.joint-name { font-size: 12px; font-weight: 600; color: var(--text); }
.joint-badge {
  font-size: 9px; padding: 2px 6px; border-radius: 4px;
  background: rgba(34,211,238,0.15); color: var(--accent);
  text-transform: uppercase; letter-spacing: 1px;
}

.axis-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.axis-label {
  font-size: 10px; font-weight: 700; width: 14px; text-align: center;
  &.x { color: var(--danger); } &.y { color: var(--accent3); } &.z { color: var(--accent2); }
}

.axis-slider {
  flex: 1; -webkit-appearance: none; appearance: none;
  height: 4px; border-radius: 2px; background: var(--panel-border); outline: none; cursor: pointer;

  &::-webkit-slider-thumb {
    -webkit-appearance: none; appearance: none;
    width: 14px; height: 14px; border-radius: 50%; cursor: grab;
  }
  &.x::-webkit-slider-thumb { background: var(--danger); }
  &.y::-webkit-slider-thumb { background: var(--accent3); }
  &.z::-webkit-slider-thumb { background: var(--accent2); }
}

.axis-value { font-size: 10px; color: var(--text-dim); width: 42px; text-align: right; font-variant-numeric: tabular-nums; }
</style>
