<script setup lang="ts">
import SessionChart from 'pages/shared/private/session/steps/third-step/SessionChart.vue';
import type { SessionSensor } from 'src/common/api/manual/constructors_api';

interface Props {
  sensors: SessionSensor[];
  viewType: 'grid' | 'unified' | 'table' | 'summary';
}

defineProps<Props>();
</script>

<template>
  <div class="column u-gap-8 u-h-min-0 u-w-min-0 u-h-100 u-w-100">
    <div
      v-if="sensors.length === 0"
      class="flex flex-col items-center justify-center text-grey-6 u-h-min-0 u-w-min-0 u-h-100 u-w-100"
    >
      <q-icon name="sensors_off" size="48px" class="q-mb-sm" />
      <div class="text-subtitle1">Nenhum sensor disponível no momento.</div>
    </div>
    <div v-else>
      <session-chart
        v-if="viewType === 'unified'"
        :sensors="sensors"
        :allowed-column="['yaw', 'pitch', 'roll']"
      />
      <div v-else-if="viewType == 'grid'" class="chart-grid u-h-min-0 u-h-100 gap-8">
        <session-chart :sensors="sensors" :allowed-column="['yaw']" />
        <session-chart :sensors="sensors" :allowed-column="['pitch']" />
        <session-chart :sensors="sensors" :allowed-column="['roll']" />
      </div>
      <div v-else-if="viewType == 'summary'" class="chart-summary">
        <p>Summary</p>
      </div>
      <div v-else-if="viewType == 'table'" class="chart-table">
        <p>Table</p>
      </div>
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
