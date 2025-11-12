<script setup lang="ts">
import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';

interface Props {
  inProgress: boolean;
  sensorList: SessionSensorDto[];
  selectedSensors: Set<SessionSensorDto>;
  addSensorListener: (sessionSensorDto: SessionSensorDto) => void;
  removeSensorListener: (sessionSensorDto: SessionSensorDto) => void;
  commandCalibrate: (sessionSensorDto: SessionSensorDto) => Promise<void>;
}

defineProps<Props>();
</script>

<template>
  <div class="column u-gap-8 u-h-min-0 u-w-min-0 u-h-100">
    <!-- Caso não haja sensores disponíveis -->
    <div v-if="sensorList.length === 0" class="text-center text-grey u-h-min-0 u-w-min-0 u-h-100">
      <q-icon name="sensors_off" size="md" class="q-mr-sm" />
      Nenhum sensor disponível no momento.
    </div>

    <!-- Caso existam sensores, mas nenhum esteja selecionado -->
    <div
      v-else-if="selectedSensors.size === 0"
      class="row text-grey justify-center u-w-min-0 u-w-100 u-p-8 items-center u-gap-8"
    >
      <q-icon name="info" size="md" />
      Nenhum sensor conectado.
    </div>

    <q-card
      flat
      bordered
      class="u-p-12 u-w-min-0"
      v-for="(sensor, index) in sensorList"
      :key="index"
    >
      <div class="row justify-between q-mb-sm">
        <span
          v-if="Array.from(selectedSensors).some((s) => s.ip === sensor.ip)"
          class="text-positive"
        >
          Conectado
        </span>
        <span class="f-bold"><b>Nome:</b> {{ sensor.name || 'Sem nome' }}</span>
        <span class="f-bold">
          <b>IP: </b>
          <a :href="sensor.ip" target="_blank">{{ sensor.ip }}</a>
        </span>
      </div>

      <div class="row justify-between">
        <q-btn
          rounded
          dense
          unelevated
          label="Calibrar"
          size="md"
          class="row"
          icon="settings"
          @click="() => commandCalibrate(sensor)"
        />

        <q-btn
          rounded
          dense
          unelevated
          label="Adicionar sensor"
          size="md"
          class="row"
          icon="done"
          @click="() => addSensorListener(sensor)"
        />

        <q-btn
          dense
          flat
          rounded
          unelevated
          label="Remover sensor"
          size="md"
          class="row"
          color="primary"
          icon="close"
          @click="() => removeSensorListener(sensor)"
        />
      </div>
    </q-card>
  </div>
</template>

<style scoped lang="scss">
.text-center {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  font-size: 1.1rem;
}
</style>
