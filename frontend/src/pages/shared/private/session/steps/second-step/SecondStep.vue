<script setup lang="ts">
import type { SessionSensorDto } from 'src/common/models/socket/SessionSensorDto';
import { Sensor } from 'src/common/models/sensor/Sensor';
import { AddSensorDto, MessageAddSensorDto } from 'src/common/models/socket/MessageAddSensorDto';
import { socket } from 'boot/socket';
import type { MessageClientMeasurementBlock } from 'src/common/models/socket/MessageClientMeasurementBlock';
import { computed } from 'vue';
import type { Movement } from 'src/common/models/movement/Movement';
import {
  MessageRemoveSensorDto,
  RemoveSensorDto,
} from 'src/common/models/socket/MessageRemoveSensorDto';
import {
  CalibrateSensorDto,
  MessageCalibrateSensorDto,
} from 'src/common/models/socket/MessageCalibrateSensorDto';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

interface Props {
  inProgress: boolean;
  availableSensorList: SessionSensorDto[];
  selectedSensorList: Set<SessionSensorDto>;
  selectedMovement?: Movement | undefined;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:selectedSensorList', val: Set<SessionSensorDto>): void;
  (e: 'update:selectedMovement', val: Movement | undefined): void;
}>();

const selectedSensorList = computed({
  get: () => props.selectedSensorList,
  set: (val) => emit('update:selectedSensorList', val),
});
const selectedMovement = computed({
  get: () => props.selectedMovement,
  set: (val) => emit('update:selectedMovement', val),
});

async function addSensorListener(sessionSensorDto: SessionSensorDto) {
  if (!sessionSensorDto.mac) return;

  const sensors = Array.from(selectedSensorList.value);
  const alreadyExists = sensors.some((s) => s.mac === sessionSensorDto.mac);
  if (alreadyExists) return;

  const sensor = new Sensor();
  sensor.ip = sessionSensorDto.ip ?? '';
  sensor.macAddress = sessionSensorDto.mac ?? '';
  sensor.sensorName = sessionSensorDto.name ?? '';
  selectedSensorList.value.add(sensor);

  const addSensor = new MessageAddSensorDto();
  const content = new AddSensorDto();
  content.sensor = sessionSensorDto.clientId ?? '';
  addSensor.content = content;
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_ADD_SENSOR, addSensor);
  if (r == 'JOINED_ROOM') {
    const event = `${SocketEvents.SERVER_CLIENT_MEASUREMENT}:${sessionSensorDto.mac}`;
    socket.on(event, (data: MessageClientMeasurementBlock) => {
      const block = data.content;
      console.log(SocketEvents.SERVER_CLIENT_MEASUREMENT, data);
      const sensors = selectedMovement.value?.sensors ?? [];
      const sensorIndex = sensors.findIndex((s) => s.macAddress == sessionSensorDto.mac);
      if (sensorIndex != -1) {
        const sensor = selectedMovement.value?.sensors?.[sensorIndex];
        if (sensor) {
          selectedMovement.value!.sensors[sensorIndex]!.measurements =
            sensor.measurements.concat(block);
        }
      }
    });
  }
}

async function removeSensorListener(sessionSensorDto: SessionSensorDto) {
  if (!sessionSensorDto.mac) return;

  const sensors = Array.from(selectedSensorList.value);
  const alreadyExists = sensors.some((s) => s.mac === sessionSensorDto.mac);
  const alreadyExistsIndex = sensors.findIndex((s) => s.mac === sessionSensorDto.mac);
  if (!alreadyExists) return;

  const removeSensor = new MessageRemoveSensorDto();
  const content = new RemoveSensorDto();
  content.sensor = sessionSensorDto.clientId ?? '';
  removeSensor.content = content;

  const sensorToRemove = sensors[alreadyExistsIndex];
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_REMOVE_SENSOR, removeSensor);
  if (r == 'REMOVED_ROOM' && sensorToRemove) {
    const event = `${SocketEvents.SERVER_CLIENT_MEASUREMENT}:${sessionSensorDto.mac}`;
    socket.removeListener(event);
    selectedSensorList.value.delete(sensorToRemove);
  }
}

async function commandCalibrate(sessionSensorDto: SessionSensorDto) {
  const message = new MessageCalibrateSensorDto();
  const content = new CalibrateSensorDto();
  content.sensor = sessionSensorDto.clientId!;
  message.content = content;
  await socket.emitWithAck(SocketEvents.CLIENT_SERVER_CALIBRATE, message);
}
</script>

<template>
  <div class="column u-gap-12 u-w-100 u-h-100">
    <div
      v-if="availableSensorList.length === 0"
      class="flex flex-col items-center justify-center text-grey-6 u-h-min-0 u-w-min-0 u-h-100 u-w-100"
    >
      <q-icon name="sensors_off" size="48px" class="q-mb-sm" />
      <div class="text-subtitle1">Nenhum sensor disponível no momento.</div>
    </div>

    <div
      v-else-if="selectedSensorList.size === 0"
      class="flex items-center justify-center text-grey-7 q-py-lg q-gutter-sm"
    >
      <q-icon name="info" size="md" />
      <div>Nenhum sensor conectado.</div>
    </div>

    <q-card
      v-for="(sensor, index) in availableSensorList"
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
            v-if="Array.from(selectedSensorList).some((s) => s.ip === sensor.ip)"
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
          v-if="!Array.from(selectedSensorList).some((s) => s.ip === sensor.ip)"
          rounded
          flat
          color="primary"
          icon="done"
          label="Adicionar"
          @click="() => addSensorListener(sensor)"
        />
        <q-btn
          v-if="Array.from(selectedSensorList).some((s) => s.ip === sensor.ip)"
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
