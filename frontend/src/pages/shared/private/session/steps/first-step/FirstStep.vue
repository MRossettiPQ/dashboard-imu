<script setup lang="ts">
import type { SessionStore } from 'pages/shared/private/session/utils/SessionStore';
import { computed, ref, watch } from 'vue';
import type { ProcedureType } from 'src/common/models/procedure/Procedure';
import type { QTableColumn } from 'quasar';
import type { MovementType } from 'src/common/models/movement/Movement';

interface Props {
  session: SessionStore;
}

const selectedProcedure = ref<ProcedureType | undefined>();
const selectedMovements = ref<MovementType[]>([]);
const props = defineProps<Props>();
const emit = defineEmits<(e: 'update:session', val: SessionStore) => void>();
const session = computed({
  get: () => props.session,
  set: (val) => emit('update:session', val),
});

// Reset selected movements quando o procedimento muda
watch(selectedProcedure, () => {
  selectedMovements.value = [];
});

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
</script>

<template>
  <div class="column u-gap-12 h-100 u-h-min-0" style="height: 100%">
    <q-card flat bordered class="column w-100 u-gap-8 u-p-8" v-if="session?.procedureTypes">
      <q-select
        v-model="selectedProcedure"
        :options="session.procedureTypes"
        emit-value
        dense
        filled
        label="Procedimento"
        option-label="description"
        option-value="value"
      />

      <q-btn
        v-if="selectedProcedure && selectedMovements.length > 0"
        class="col"
        unelevated
        color="primary"
        dense
        icon="las la-plus"
        :label="`Adicionar ${selectedMovements.length} movimento(s)`"
        @click="session.addProcedure(selectedProcedure, selectedMovements)"
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
