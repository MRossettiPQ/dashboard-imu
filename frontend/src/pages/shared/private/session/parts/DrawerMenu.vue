<script setup lang="ts">
import { computed } from 'vue';
import type { Session } from 'src/common/models/session/Session';
import type { TableColumn } from 'src/common/models/models';
import { Movement } from 'src/common/models/movement/Movement';
import { findMovementEnum } from 'src/common/models/movement/Movement';
import type { Procedure } from 'src/common/models/procedure/Procedure';
import { findProcedureEnum } from 'src/common/models/procedure/Procedure';
import type { Sensor } from 'src/common/models/sensor/Sensor';
import { positionOptions } from 'src/common/models/sensor/Sensor';

interface Props {
  session: Session;
  rightDrawer: boolean;
  selectedSensorList: Set<Sensor>;
  selectedProcedure?: Procedure | undefined;
  selectedMovement?: Movement | undefined;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:rightDrawer', val: boolean): void;
  (e: 'update:selectedProcedure', val: Procedure | undefined): void;
  (e: 'update:selectedMovement', val: Movement | undefined): void;
  (e: 'update:session', val: Session): void;
}>();

const rightDrawer = computed({
  get: () => props.rightDrawer,
  set: (val) => emit('update:rightDrawer', val),
});
const session = computed({
  get: () => props.session,
  set: (val) => emit('update:session', val),
});
const selectedProcedure = computed({
  get: () => props.selectedProcedure,
  set: (val) => emit('update:selectedProcedure', val),
});
const selectedMovement = computed({
  get: () => props.selectedMovement,
  set: (val) => emit('update:selectedMovement', val),
});

const movementColumns: TableColumn<Movement>[] = [
  {
    name: 'name',
    align: 'left',
    label: 'Nome',
    field: 'type',
    format: (val: unknown) => {
      if (!val || typeof val !== 'string') return 'Nenhum';
      return findMovementEnum(val) ?? 'Inválido';
    },
  },
  {
    name: 'measurements',
    align: 'center',
    label: 'Leituras',
    format: (val: unknown) => {
      if (val && Array.isArray(val)) {
        const sensors = val as Sensor[];
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

const sensorColumns: TableColumn<Sensor>[] = [
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

function getTotalMeasurements(movement: Movement): number {
  if (!movement.sensors || movement.sensors.length === 0) return 0;

  return movement.sensors.reduce((total, sensor) => {
    return total + (sensor.measurements?.length || 0);
  }, 0);
}

function removeMovement(procedure: Procedure, rowIndex: number, event: Event) {
  event.stopPropagation();

  const procedureIndex = session.value.procedures.findIndex((p) => p === procedure);
  if (procedureIndex !== -1) {
    const movements = session.value.procedures[procedureIndex]!.movements;

    if (rowIndex >= 0 && rowIndex < movements.length) {
      const movementToRemove = movements[rowIndex];
      session.value.procedures[procedureIndex]!.movements.splice(rowIndex, 1);

      if (selectedMovement.value === movementToRemove) {
        selectedMovement.value = undefined;
      }
    }
  }
}

function duplicateMovement(procedure: Procedure, rowIndex: number, event: Event) {
  event.stopPropagation();

  const procedureIndex = session.value.procedures.findIndex((p) => p === procedure);
  if (procedureIndex !== -1) {
    const movements = session.value.procedures[procedureIndex]!.movements;

    if (rowIndex >= 0 && rowIndex < movements.length) {
      const movementToDuplicate = movements[rowIndex];
      if (!movementToDuplicate) return;

      const duplicatedMovement = new Movement();
      duplicatedMovement.type = movementToDuplicate.type!;

      session.value.procedures[procedureIndex]!.movements.splice(
        rowIndex + 1,
        0,
        duplicatedMovement,
      );
    }
  }
}

function removeProcedure(procedure: Procedure, event: Event) {
  event.stopPropagation();

  const index = session.value.procedures.findIndex((p) => p === procedure);
  if (index === -1) return;

  const procedureToRemove = session.value.procedures[index];

  session.value.procedures.splice(index, 1);

  // Se o atual selecionado for o removido → limpar seleção
  if (selectedProcedure.value === procedureToRemove) {
    selectedProcedure.value = undefined;
    selectedMovement.value = undefined;
  }
}

function isMovementSelected(procedure: Procedure, movement: Movement): boolean {
  return (
    selectedProcedure.value === procedure &&
    selectedMovement.value?.sessionIdentifier === movement.sessionIdentifier
  );
}

function onMovementSelect(procedure: Procedure, movement: Movement) {
  emit('update:selectedProcedure', procedure);
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

      <q-card v-if="selectedProcedure?.type" class="column u-gap-8 no-wrap u-p-12">
        <div class="row items-center u-gap-8">
          <q-icon name="assignment" color="primary" size="24px" />
          <b class="u-m-0 text-h6">Procedimento Atual</b>
        </div>

        <div class="column u-gap-6">
          <div class="row items-center justify-between">
            <span class="text-weight-medium text-grey-8">Procedimento:</span>
            <q-badge color="primary" class="text-caption">
              {{ findProcedureEnum(selectedProcedure.type) || 'Não identificado' }}
            </q-badge>
          </div>

          <div v-if="selectedMovement?.type" class="row items-center justify-between">
            <span class="text-weight-medium text-grey-8">Movimento:</span>
            <q-badge color="secondary" class="text-caption">
              {{ findMovementEnum(selectedMovement.type) || 'Não identificado' }}
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
              <q-select v-model="props.row.position" :options="positionOptions" dense borderless />
            </q-td>
          </template>
        </q-table>
      </div>

      <q-separator />

      <div v-if="session.procedures.length" class="column u-gap-12 no-wrap u-h-100 u-h-min-0">
        <b class="u-m-0">Procedimentos</b>
        <div class="column u-gap-12 overflow-auto no-wrap u-h-100">
          <div class="column u-gap-6" v-for="(procedure, index) in session.procedures" :key="index">
            <q-table
              v-if="procedure.type"
              hide-pagination
              dense
              :rows="procedure.movements"
              :columns="movementColumns"
              flat
              bordered
              @row-click="(_, row) => onMovementSelect(procedure, row)"
            >
              <template v-slot:top>
                <b class="u-m-0">
                  {{ findProcedureEnum(procedure.type) || 'Procedimento sem nome' }}
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
                      @click="removeProcedure(procedure, $event)"
                    >
                      <q-tooltip>Remover {{ findProcedureEnum(procedure.type) }}</q-tooltip>
                    </q-btn>
                  </q-btn-group>
                </div>
              </template>

              <template v-slot:body="props">
                <q-tr :props="props" @click="onMovementSelect(procedure, props.row)">
                  <q-td key="name" :props="props">
                    <div class="row items-center no-wrap">
                      <q-icon
                        v-if="isMovementSelected(procedure, props.row)"
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
                        @click="duplicateMovement(procedure, props.rowIndex, $event)"
                        :title="`Duplicar movimento`"
                      >
                        <q-tooltip> Duplicar {{ findMovementEnum(props.row.type) }} </q-tooltip>
                      </q-btn>
                      <q-btn
                        dense
                        color="negative"
                        size="sm"
                        unelevated
                        icon="delete"
                        @click="removeMovement(procedure, props.rowIndex, $event)"
                        :title="`Remover movimento`"
                      >
                        <q-tooltip> Remover {{ findMovementEnum(props.row.type) }} </q-tooltip>
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
