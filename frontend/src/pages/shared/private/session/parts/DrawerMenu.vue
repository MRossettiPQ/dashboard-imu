<script setup lang="ts">
import { computed } from 'vue';
import type { TableColumn } from 'src/common/api/manual/models';
import { Movement } from 'src/common/api/manual/constructors_api';
import type {
  ArticulationDto,
  MovementDto,
  SensorDto,
  SessionDto,
} from 'src/common/api/generated/models';
import { MovementEnum } from 'src/common/api/generated/models';

interface Props {
  session: SessionDto;
  rightDrawer: boolean;
  selectedSensorList: Set<SensorDto>;
  selectedArticulation?: ArticulationDto | undefined;
  selectedMovement?: MovementDto | undefined;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:rightDrawer', val: boolean): void;
  (e: 'update:selectedArticulation', val: ArticulationDto | undefined): void;
  (e: 'update:selectedMovement', val: MovementDto | undefined): void;
  (e: 'update:session', val: SessionDto): void;
}>();

const rightDrawer = computed({
  get: () => props.rightDrawer,
  set: (val) => emit('update:rightDrawer', val),
});
const session = computed({
  get: () => props.session,
  set: (val) => emit('update:session', val),
});
const selectedArticulation = computed({
  get: () => props.selectedArticulation,
  set: (val) => emit('update:selectedArticulation', val),
});
const selectedMovement = computed({
  get: () => props.selectedMovement,
  set: (val) => emit('update:selectedMovement', val),
});

const movementColumns: TableColumn<MovementDto>[] = [
  {
    name: 'name',
    align: 'left',
    label: 'Nome',
    field: 'type',
    format: (val: unknown) => {
      if (!val || typeof val !== 'string') return 'Nenhum';

      if (val in MovementEnum) {
        return MovementEnum[val as keyof typeof MovementEnum];
      }

      return 'Inválido';
    },
  },
  {
    name: 'measurements',
    align: 'center',
    label: 'Leituras',
    format: (val: unknown) => {
      if (val && Array.isArray(val)) {
        const sensors = val as SensorDto[];
        const size = sensors[0]?.measurements?.length ?? 0;
        return size.toString();
      }
      return '0';
    },
    field: 'sensors',
    headerStyle: 'width: 120px',
  },
  {
    name: 'actions',
    align: 'right',
    label: 'Ações',
    field: 'actions',
    headerStyle: 'width: 120px',
  },
];

const sensorColumns: TableColumn<SensorDto>[] = [
  {
    name: 'macAddress',
    align: 'left',
    label: 'MAC',
    field: 'macAddress',
  },
  {
    name: 'position',
    align: 'left',
    label: 'Posição',
    field: 'position',
  },
];

function getTotalMeasurements(movement: MovementDto): number {
  if (!movement.sensors || movement.sensors.length === 0) return 0;

  return movement.sensors.reduce((total: number, sensor: SensorDto) => {
    return total + (sensor.measurements?.length || 0);
  }, 0);
}

function removeMovement(articulation: ArticulationDto, rowIndex: number, event: Event) {
  event.stopPropagation();

  const articulations = session.value.articulations;
  const procedureIndex = articulations.findIndex((a: ArticulationDto) => a === articulation);
  if (procedureIndex !== -1) {
    const movements = session.value.articulations[procedureIndex]!.movements;

    if (rowIndex >= 0 && rowIndex < movements.length) {
      const movementToRemove = movements[rowIndex];
      session.value.articulations[procedureIndex]!.movements.splice(rowIndex, 1);

      if (selectedMovement.value === movementToRemove) {
        selectedMovement.value = undefined;
      }
    }
  }
}

function duplicateMovement(articulation: ArticulationDto, rowIndex: number, event: Event) {
  event.stopPropagation();

  const articulations = session.value.articulations;
  const procedureIndex = articulations.findIndex((a: ArticulationDto) => a === articulation);
  if (procedureIndex !== -1) {
    const movements = articulations[procedureIndex]!.movements;

    if (rowIndex >= 0 && rowIndex < movements.length) {
      const movementToDuplicate = movements[rowIndex];
      if (!movementToDuplicate) return;

      const duplicatedMovement = new Movement();

      session.value.articulations[procedureIndex]!.movements.splice(
        rowIndex + 1,
        0,
        duplicatedMovement,
      );
    }
  }
}

function removeProcedure(articulation: ArticulationDto, event: Event) {
  event.stopPropagation();

  const articulations = session.value.articulations;
  const procedureIndex = articulations.findIndex((a: ArticulationDto) => a === articulation);
  if (procedureIndex === -1) return;

  const procedureToRemove = session.value.articulations[procedureIndex];

  session.value.articulations.splice(procedureIndex, 1);

  // Se o atual selecionado for o removido → limpar seleção
  if (selectedArticulation.value === procedureToRemove) {
    selectedArticulation.value = undefined;
    selectedMovement.value = undefined;
  }
}

function isMovementSelected(procedure: ArticulationDto, movement: MovementDto): boolean {
  return selectedArticulation.value === procedure && selectedMovement.value?.id === movement.id;
}

function onMovementSelect(procedure: ArticulationDto, movement: Movement) {
  emit('update:selectedArticulation', procedure);
  emit('update:selectedMovement', movement);
}
</script>

<template>
  <q-drawer
    bordered
    side="right"
    :modelValue="rightDrawer"
    class="custom-drawer"
    content-class="bg-grey-1 column justify-between no-wrap u-h-100"
  >
    <q-card class="u-p-12 column u-gap-12 no-wrap u-h-100">
      <q-btn
        rounded
        dense
        unelevated
        size="md"
        label="Add medições"
        class="row icon-primary"
        icon="settings"
      />

      <q-separator />

      <q-card v-if="selectedArticulation?.type" class="column u-gap-8 no-wrap u-p-12">
        <div class="row items-center u-gap-8">
          <q-icon name="assignment" color="primary" size="24px" />
          <b class="u-m-0 text-h6">Procedimento Atual</b>
        </div>

        <div class="column u-gap-6">
          <div class="row items-center justify-between">
            <span class="text-weight-medium text-grey-8">Procedimento:</span>
            <q-badge color="primary" class="text-caption">
              {{ selectedArticulation.type?.type || 'Não identificado' }}
            </q-badge>
          </div>

          <div v-if="selectedMovement?.type" class="row items-center justify-between">
            <span class="text-weight-medium text-grey-8">Movimento:</span>
            <q-badge color="secondary" class="text-caption">
              {{ selectedMovement.type || 'Não identificado' }}
            </q-badge>
          </div>

          <div v-if="selectedMovement" class="column u-gap-4 u-mt-8">
            <q-separator />
            <div class="text-caption text-weight-medium text-grey-7 u-mb-4">Estatísticas:</div>

            <div class="row items-center justify-between">
              <span class="text-caption text-grey-7">Sensores ativos:</span>
              <span class="text-caption text-weight-medium">
                {{ selectedMovement.sensors?.length || 0 }}
              </span>
            </div>

            <div class="row items-center justify-between">
              <span class="text-caption text-grey-7">Total de leituras:</span>
              <span class="text-caption text-weight-medium">
                {{ getTotalMeasurements(selectedMovement) }}
              </span>
            </div>
          </div>

          <div v-else class="column items-center u-gap-4 u-mt-8">
            <q-separator />
            <q-icon name="info" color="amber" size="20px" />
            <span class="text-caption text-amber text-center">
              Selecione um movimento para visualizar as medições
            </span>
          </div>
        </div>
      </q-card>

      <div v-if="selectedSensorList.size" class="column u-gap-12 no-wrap">
        <b class="u-m-0">Sensores</b>
        <q-table
          hide-pagination
          dense
          :rows="Array.from(selectedSensorList)"
          :columns="sensorColumns"
          flat
          bordered
        >
          <template v-slot:body-cell-position="props">
            <q-td :props="props">
              <q-select v-model="props.row.position" :options="[]" dense borderless />
            </q-td>
          </template>
        </q-table>
      </div>

      <q-separator />

      <div v-if="session.articulations.length" class="column u-gap-12 no-wrap u-h-100 u-h-min-0">
        <b class="u-m-0">Procedimentos</b>
        <div class="column u-gap-12 overflow-auto no-wrap u-h-100">
          <div
            class="column u-gap-6"
            v-for="(articulation, index) in session.articulations"
            :key="index"
          >
            <q-table
              v-if="articulation.type"
              hide-pagination
              dense
              :rows="articulation.movements"
              :columns="movementColumns"
              flat
              bordered
              @row-click="(_, row) => onMovementSelect(articulation, row)"
            >
              <template v-slot:top>
                <b class="u-m-0">
                  {{ articulation.type?.type || 'Procedimento sem nome' }}
                </b>
                <q-space />

                <div>
                  <q-btn-group flat>
                    <q-btn
                      dense
                      color="negative"
                      size="sm"
                      unelevated
                      icon="delete"
                      :title="`Remover procedimento`"
                      @click="removeProcedure(articulation, $event)"
                    >
                      <q-tooltip>Remover {{ articulation.type?.type }}</q-tooltip>
                    </q-btn>
                  </q-btn-group>
                </div>
              </template>

              <template v-slot:body="props">
                <q-tr :props="props" @click="onMovementSelect(articulation, props.row)">
                  <q-td key="name" :props="props">
                    <div class="row items-center no-wrap">
                      <q-icon
                        v-if="isMovementSelected(articulation, props.row)"
                        name="check_circle"
                        color="positive"
                        size="16px"
                        class="q-mr-xs"
                      />
                      {{ props.cols[0].value }}
                    </div>
                  </q-td>
                  <q-td key="measurements" :props="props">
                    {{ props.cols[1].value }}
                  </q-td>
                  <q-td key="actions" :props="props">
                    <q-btn-group flat>
                      <q-btn
                        dense
                        color="primary"
                        size="sm"
                        unelevated
                        icon="content_copy"
                        @click="duplicateMovement(articulation, props.rowIndex, $event)"
                        :title="`Duplicar movimento`"
                      >
                        <q-tooltip> Duplicar {{ props.row.type }} </q-tooltip>
                      </q-btn>
                      <q-btn
                        dense
                        color="negative"
                        size="sm"
                        unelevated
                        icon="delete"
                        @click="removeMovement(articulation, props.rowIndex, $event)"
                        :title="`Remover movimento`"
                      >
                        <q-tooltip> Remover {{ props.row.type }} </q-tooltip>
                      </q-btn>
                    </q-btn-group>
                  </q-td>
                </q-tr>
              </template>
            </q-table>
          </div>
        </div>
      </div>

      <q-separator />
    </q-card>
  </q-drawer>
</template>

<style scoped lang="scss">
.q-drawer {
  top: 0 !important;
}
</style>
