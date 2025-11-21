<script setup lang="ts">
import SessionChart from 'pages/shared/private/session/steps/third-step/SessionChart.vue';
import type { Movement } from 'src/common/models/movement/Movement';
import type { Procedure } from 'src/common/models/procedure/Procedure';

interface Props {
  selectedProcedure: Procedure | undefined;
  selectedMovement: Movement | undefined;
  viewType: 'grid' | 'unified' | 'table' | 'summary';
  commandStart: () => void;
  commandRestart: () => void;
  commandStop: () => void;
}

defineProps<Props>();
</script>

<template>
  <div class="column u-gap-8 u-h-min-0 u-w-min-0 u-h-100 u-w-100">
    <div
      v-if="selectedMovement?.sensors.length === 0"
      class="flex flex-col items-center justify-center text-grey-6 u-h-min-0 u-w-min-0 u-h-100 u-w-100"
    >
      <q-icon name="sensors_off" size="48px" class="q-mb-sm" />
      <div class="text-subtitle1">Nenhum sensor disponível no momento.</div>
    </div>
    <div v-else>
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
