<script setup lang="ts">
import type { SessionStore } from 'pages/shared/private/session/utils/SessionStore';
import SessionChart from 'pages/shared/private/session/steps/third-step/SessionChart.vue';

interface Props {
  session: SessionStore;
}

defineProps<Props>();
</script>

<template>
  <div>
    <p>Third step</p>
    <session-chart
      v-if="session.selectedMovement && session.viewType == 'unified'"
      :sensors="session.selectedMovement.sensors"
      :allowed-column="['yaw', 'pitch', 'roll']"
    />
    <div v-else-if="session.selectedMovement && session.viewType == 'grid'" class="chart-grid">
      <session-chart :sensors="session.selectedMovement.sensors" :allowed-column="['yaw']" />
      <session-chart :sensors="session.selectedMovement.sensors" :allowed-column="['pitch']" />
      <session-chart :sensors="session.selectedMovement.sensors" :allowed-column="['roll']" />
    </div>
    <div
      v-else-if="session.selectedMovement && session.viewType == 'summary'"
      class="chart-summary"
    >
      <p>Summary</p>
    </div>
    <div v-else-if="session.selectedMovement && session.viewType == 'table'" class="chart-table">
      <p>Table</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}
</style>
