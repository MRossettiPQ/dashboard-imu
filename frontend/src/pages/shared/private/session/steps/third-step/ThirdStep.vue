<script setup lang="ts">
import SessionChart from 'pages/shared/private/session/steps/third-step/SessionChart.vue';
import type { Movement } from 'src/common/models/movement/Movement';

interface Props {
  selectedMovement: Movement | undefined;
  viewType: 'grid' | 'unified' | 'table' | 'summary';
  commandStart: () => void;
  commandRestart: () => void;
  commandStop: () => void;
}

defineProps<Props>();
</script>

<template>
  <div class="column u-gap-8 u-h-min-0 u-w-min-0 u-h-100">
    <session-chart
      v-if="selectedMovement && viewType === 'unified'"
      :sensors="selectedMovement.sensors"
      :allowed-column="['yaw', 'pitch', 'roll']"
    />
    <div
      v-else-if="selectedMovement && viewType == 'grid'"
      class="chart-grid u-h-min-0 u-h-100 gap-8"
    >
      <session-chart :sensors="selectedMovement.sensors" :allowed-column="['yaw']" />
      <session-chart :sensors="selectedMovement.sensors" :allowed-column="['pitch']" />
      <session-chart :sensors="selectedMovement.sensors" :allowed-column="['roll']" />
    </div>
    <div v-else-if="selectedMovement && viewType == 'summary'" class="chart-summary">
      <p>Summary</p>
    </div>
    <div v-else-if="selectedMovement && viewType == 'table'" class="chart-table">
      <p>Table</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chart-grid {
  display: grid;
  grid-template-columns: 1fr;
  row-gap: 16px;
  width: 100%;
}
</style>
