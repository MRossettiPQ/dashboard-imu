<script setup lang="ts">
import { computed, ref } from 'vue';
import type { QTableColumn } from 'quasar';
import type {
  ArticulationTypeDto,
  MovementTypeDto,
  SessionDto,
} from 'src/common/api/generated/models';
import { Articulation, Movement } from 'src/common/api/manual/constructors_api';

interface Props {
  session: SessionDto;
  availableArticulationList: ArticulationTypeDto[];
}

const props = defineProps<Props>();

const selectedArticulation = ref<ArticulationTypeDto | undefined>();
const selectedMovements = ref<MovementTypeDto[]>([]);
const emit = defineEmits<{
  (e: 'update:availableArticulationList', val: ArticulationTypeDto[]): void;
  (e: 'update:session', val: SessionDto): void;
}>();

const availableArticulationList = computed({
  get: () => props.availableArticulationList,
  set: (val) => emit('update:availableArticulationList', val),
});
const session = computed({
  get: () => props.session,
  set: (val) => emit('update:session', val),
});

function addProcedure(
  articulationType: ArticulationTypeDto | undefined,
  movementTypes: MovementTypeDto[],
): void {
  if (!articulationType?.type) {
    return;
  }

  let isNewProcedure = false;
  let articulation = session.value.articulations.find((p) => p.type === articulationType.type);

  if (!articulation) {
    isNewProcedure = true;
    articulation = new Articulation();
  }

  const type = availableArticulationList.value.find((a) => a.type == articulation?.type);

  if (!type) {
    return;
  }

  articulation.type = type;

  const existingMovementsByType = new Map<string, Movement[]>();
  articulation.movements.forEach((movement) => {
    if (movement.type) {
      if (!existingMovementsByType.has(movement.type)) {
        existingMovementsByType.set(movement.type, []);
      }
      existingMovementsByType.get(movement.type)!.push(new Movement(movement));
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
      newMovements.push(movement);
    }
  }

  articulation.movements = newMovements;

  if (isNewProcedure) {
    session.value.articulations.push(articulation);
  }
}

const toggleMovementSelection = (movement: MovementTypeDto) => {
  const index = selectedMovements.value.findIndex((m) => m.id === movement.id);
  if (index > -1) {
    selectedMovements.value.splice(index, 1);
  } else {
    selectedMovements.value.push(movement);
  }
};

const isMovementSelected = (movement: MovementTypeDto) => {
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
  const selectedType = selectedArticulation.value;
  const procedureType = props.availableArticulationList.find((p) => p.type === selectedType?.type);
  const sessionProcedure = props.session.articulations.find((p) => p.type === selectedType?.type);

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
    <q-card
      flat
      bordered
      class="flex column u-w-100 u-gap-8 u-p-8"
      v-if="availableArticulationList"
    >
      <q-select
        v-model="selectedArticulation"
        :options="availableArticulationList"
        emit-value
        dense
        filled
        label="Procedimento"
        option-label="description"
        option-value="value"
        @update:model-value="onSelectProcedure"
      />

      <q-btn
        v-if="selectedArticulation && selectedMovements.length > 0"
        class="col"
        unelevated
        color="primary"
        dense
        icon="las la-plus"
        :label="`Adicionar procedimento e ${selectedMovements.length} movimento(s)`"
        @click="() => addProcedure(selectedArticulation, selectedMovements)"
      />

      <q-banner
        v-else-if="selectedArticulation && selectedMovements.length === 0"
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
      v-if="selectedArticulation"
      class="col column u-gap-8 u-h-min-0 u-w-min-0 movement-table"
      flat
      bordered
      :rows="selectedArticulation.movementsTypes"
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
              {{ props.row.type }}
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
