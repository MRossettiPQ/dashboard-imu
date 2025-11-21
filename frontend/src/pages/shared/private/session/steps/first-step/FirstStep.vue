<script setup lang="ts">
import { computed, ref } from 'vue';
import type { ProcedureType } from 'src/common/models/procedure/Procedure';
import { Procedure } from 'src/common/models/procedure/Procedure';
import type { QTableColumn } from 'quasar';
import type { MovementType } from 'src/common/models/movement/Movement';
import { findMovementEnum, Movement } from 'src/common/models/movement/Movement';
import type { Session } from 'src/common/models/session/Session';

interface Props {
  session: Session;
  procedureTypes: ProcedureType[];
}

const props = defineProps<Props>();

const selectedProcedure = ref<ProcedureType | undefined>();
const selectedMovements = ref<MovementType[]>([]);
const emit = defineEmits<{
  (e: 'update:procedureTypes', val: ProcedureType[]): void;
  (e: 'update:session', val: Session): void;
}>();

const procedureTypes = computed({
  get: () => props.procedureTypes,
  set: (val) => emit('update:procedureTypes', val),
});
const session = computed({
  get: () => props.session,
  set: (val) => emit('update:session', val),
});

function addProcedure(
  procedureType: ProcedureType | undefined,
  movementTypes: MovementType[],
): void {
  if (!procedureType?.type) {
    return;
  }

  let isNewProcedure = false;
  let procedure = session.value.procedures.find((p) => p.type === procedureType.type);

  if (!procedure) {
    isNewProcedure = true;
    procedure = new Procedure();
  }

  procedure.type = procedureType.type;

  const existingMovementsByType = new Map<string, Movement[]>();
  procedure.movements.forEach((movement) => {
    if (movement.type) {
      if (!existingMovementsByType.has(movement.type)) {
        existingMovementsByType.set(movement.type, []);
      }
      existingMovementsByType.get(movement.type)!.push(movement);
    }
  });

  const newMovements: Movement[] = [];
  for (const movementType of movementTypes) {
    if (!movementType.type) {
      continue;
    }

    const existingMovements = existingMovementsByType.get(movementType.type) || [];
    if (existingMovements.length > 0) {
      newMovements.push(...existingMovements);
    } else {
      const movement = new Movement();
      movement.type = movementType.type;
      newMovements.push(movement);
    }
  }

  procedure.movements = newMovements;

  if (isNewProcedure) {
    session.value.procedures.push(procedure);
  }
}

const toggleMovementSelection = (movement: MovementType) => {
  const index = selectedMovements.value.findIndex((m) => m.id === movement.id);
  if (index > -1) {
    selectedMovements.value.splice(index, 1);
  } else {
    selectedMovements.value.push(movement);
  }
};

const isMovementSelected = (movement: MovementType) => {
  return selectedMovements.value.some((m) => m.id === movement.id);
};

const columns: QTableColumn[] = [
  {
    name: 'description',
    align: 'left',
    label: 'Descrição',
    field: 'description',
    sortable: true,
  },
  {
    name: 'imageName',
    align: 'center',
    label: 'Imagem',
    field: 'imageName',
  },
  {
    name: 'selected',
    align: 'center',
    label: '#',
    field: 'selected',
    headerStyle: 'width: 50px;',
  },
];

function onSelectProcedure() {
  selectedMovements.value = [];
  const selectedType = selectedProcedure.value;
  const procedureType = props.procedureTypes.find((p) => p.type === selectedType?.type);
  const sessionProcedure = props.session.procedures.find((p) => p.type === selectedType?.type);

  if (procedureType && sessionProcedure) {
    selectedMovements.value =
      procedureType.movementsTypes?.filter((m) =>
        sessionProcedure.movements.some((pm) => pm.type === m.type),
      ) ?? [];
  }
}
</script>

<template>
  <div class="flex column u-gap-12 u-h-min-0 u-w-min-0 u-w-100 u-h-100">
    <q-card flat bordered class="flex column u-w-100 u-gap-8 u-p-8" v-if="procedureTypes">
      <q-select
        v-model="selectedProcedure"
        :options="procedureTypes"
        emit-value
        dense
        filled
        label="Procedimento"
        option-label="description"
        option-value="value"
        @update:model-value="onSelectProcedure"
      />

      <q-btn
        v-if="selectedProcedure && selectedMovements.length > 0"
        class="col"
        unelevated
        color="primary"
        dense
        icon="las la-plus"
        :label="`Adicionar procedimento e ${selectedMovements.length} movimento(s)`"
        @click="() => addProcedure(selectedProcedure, selectedMovements)"
      />

      <q-banner
        v-else-if="selectedProcedure && selectedMovements.length === 0"
        class="bg-warning text-white"
        dense
      >
        <template v-slot:avatar>
          <q-icon name="las la-exclamation-triangle" />
        </template>
        Selecione pelo menos um movimento para adicionar o procedimento.
      </q-banner>
    </q-card>

    <q-table
      v-if="selectedProcedure"
      class="col column u-gap-8 u-h-min-0 u-w-min-0 movement-table"
      flat
      bordered
      :rows="selectedProcedure.movementsTypes"
      :columns="columns"
      row-key="id"
      hide-pagination
      :pagination="{ rowsPerPage: 0 }"
      binary-state-sort
    >
      <template v-slot:body="props">
        <q-tr :props="props" @click="toggleMovementSelection(props.row)">
          <q-td key="description" :props="props">
            {{ props.row.description }}
          </q-td>
          <q-td key="type" :props="props">
            <q-badge color="primary">
              {{ findMovementEnum(props.row.type) }}
            </q-badge>
          </q-td>
          <q-td key="imageName" :props="props">
            <q-img
              class="img-div"
              :src="`/procedures/${props.row.imageName}`"
              :alt="props.row.description"
            />
          </q-td>
          <q-td key="selected" :props="props">
            <q-checkbox
              :model-value="isMovementSelected(props.row)"
              @update:model-value="toggleMovementSelection(props.row)"
              color="primary"
            />
          </q-td>
        </q-tr>
      </template>

      <template v-slot:no-data>
        <div class="full-width row flex-center text-grey q-gutter-sm">
          <q-icon size="2em" name="las la-frown" />
          <span>Nenhum movimento disponível para este procedimento</span>
        </div>
      </template>
    </q-table>

    <q-card v-else flat bordered class="flex flex-center col u-p-20 u-h-min-0 h-100">
      <div class="text-grey text-center">
        <q-icon name="las la-hand-pointer" size="2em" />
        <div>Selecione um procedimento para ver os movimentos disponíveis</div>
      </div>
    </q-card>
  </div>
</template>

<style scoped lang="scss">
.movement-table {
  min-height: 0;
  min-width: 0;
  height: 100%;
  width: 100%;

  thead tr th {
    position: sticky;
    z-index: 1;
  }

  thead tr:first-child th {
    top: 0;
  }
}

.img-div {
  min-width: 0;
  width: 100%;
  height: auto;
  min-height: 150px;
  max-width: 255px;
  border-radius: 8px;
}
</style>
