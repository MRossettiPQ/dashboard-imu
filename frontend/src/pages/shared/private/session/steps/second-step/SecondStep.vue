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
  <div class="column u-gap-12 u-w-100 u-h-100">
    <div
      v-if="sensorList.length === 0"
      class="flex flex-col items-center justify-center text-grey-6 u-h-min-0 u-w-min-0 u-h-100 u-w-100"
    >
      <q-icon name="sensors_off" size="48px" class="q-mb-sm" />
      <div class="text-subtitle1">Nenhum sensor disponível no momento.</div>
    </div>

    <div
      v-else-if="selectedSensors.size === 0"
      class="flex items-center justify-center text-grey-7 q-py-lg q-gutter-sm"
    >
      <q-icon name="info" size="md" />
      <div>Nenhum sensor conectado.</div>
    </div>

    <q-card
      v-for="(sensor, index) in sensorList"
      :key="index"
      flat
      bordered
      class="q-pa-md rounded-2xl shadow-sm hover:shadow-md transition-all u-gap-16 column"
    >
      <div class="row items-center justify-between">
        <div class="row items-center q-gutter-sm">
          <q-icon name="sensors" color="primary" size="sm" />
          <div class="text-weight-bold text-primary">
            {{ sensor.name || 'Sem nome' }}
          </div>
        </div>

        <div class="row items-center q-gutter-xs">
          <q-icon name="lan" color="grey-7" size="sm" />
          <a :href="`http://${sensor.ip}`" target="_blank" class="text-blue-8 text-weight-medium">
            {{ sensor.ip }}
          </a>
        </div>

        <div class="row items-center">
          <q-chip
            v-if="Array.from(selectedSensors).some((s) => s.ip === sensor.ip)"
            color="positive"
            text-color="white"
            icon="check_circle"
            label="Conectado"
          />
          <q-chip
            v-else
            color="grey-5"
            text-color="black"
            icon="highlight_off"
            label="Desconectado"
          />
        </div>
      </div>

      <div class="row justify-between q-gutter-sm">
        <q-btn
          rounded
          flat
          color="secondary"
          icon="settings"
          label="Calibrar"
          @click="() => commandCalibrate(sensor)"
        />
        <q-btn
          v-if="!Array.from(selectedSensors).some((s) => s.ip === sensor.ip)"
          rounded
          flat
          color="primary"
          icon="done"
          label="Adicionar"
          @click="() => addSensorListener(sensor)"
        />
        <q-btn
          v-if="Array.from(selectedSensors).some((s) => s.ip === sensor.ip)"
          rounded
          color="negative"
          flat
          icon="close"
          label="Remover"
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
