<script setup lang="ts" generic="T, R extends { page: number; rpp: number }">
import type { QTableProps } from 'quasar';
import type PaginationUtils from 'src/common/utils/PaginationUtils';
import { computed } from 'vue';

// T = tipo dos dados por linha, ex: Record<string, any> ou algo mais específico
interface Props<T, R extends { page: number; rpp: number }> extends QTableProps {
  service: PaginationUtils<T, R>;
  editable?: boolean;
  title?: string;
  bordered?: boolean;
  dense?: boolean;
  flat?: boolean;
  rowKey?: string;
  columns: QTableProps['columns'];
  orderType?: 'ad' | 'da';
}

async function sortBy(): Promise<void> {}

const emit = defineEmits<{
  (e: 'update:params', val: typeof props.service.params): void;
}>();

const paginationInfo = computed({
  get: () => props.service.params,
  set: (val) => emit('update:params', val),
});

const props = defineProps<Props<T, R>>();
</script>

<template>
  <q-table
    :bordered="props.bordered"
    :columns="props.columns"
    :flat="props.flat"
    :dense="props.dense"
    :row-key="props.rowKey"
    :rows="props.service.items"
    :loading="props.service.loading"
    v-model:pagination="paginationInfo"
    :rows-per-page-options="[props.service.params.rpp]"
    :column-sort-order="props.orderType"
    binary-state-sort
    @request="sortBy"
    :title="props.title"
    virtual-scroll
    no-data-label="Não encontrei nada para você"
    no-results-label="O filtro não encontrou nenhum resultado"
  >
    <template v-slot:header="props">
      <q-tr :props="props">
        <q-th v-for="col in props.cols" :key="col.name" :props="props">
          {{ col.label }}
        </q-th>
        <q-th v-if="editable" auto-width />
      </q-tr>
    </template>

    <template v-slot:body="props">
      <q-tr :props="props">
        <q-td v-for="col in props.cols" :key="col.name" :props="props">
          <div v-if="col.type == 'btn'">
            <q-btn
              v-if="col.value?.show"
              :icon="col.value?.icon"
              :label="col.value?.label"
              :disable="col.value?.disable"
              :loading="col.value?.loading"
              @click="col.value?.click(col.value)"
              size="sm"
              round
              outline
              dense
            />
          </div>
          <div v-else>{{ col.value }}</div>
        </q-td>
        <q-td v-if="editable" auto-width>
          <q-btn size="sm" round outline dense icon="edit" />
        </q-td>
      </q-tr>
    </template>

    <template v-slot:bottom>
      <div class="row w-100 items-center justify-between">
        <div>Página {{ service.result.page }} de {{ service.result.pageCount }}</div>

        <div class="row">
          <q-btn
            v-if="service.result.page > 2"
            icon="first_page"
            color="grey-8"
            round
            dense
            flat
            :disable="service.result.page == 1"
            @click.prevent="service.loadFirstPage"
          />

          <q-btn
            icon="chevron_left"
            color="grey-8"
            round
            dense
            flat
            :disable="service.result.page == 1"
            @click.prevent="service.loadPrev"
          />

          <q-btn
            icon="chevron_right"
            color="grey-8"
            round
            dense
            flat
            :disable="service.result.page === service.result.pageCount || !service.hasMore"
            @click.prevent="service.loadNext"
          />

          <q-btn
            v-if="service.result.pageCount > 2"
            icon="last_page"
            color="grey-8"
            round
            dense
            flat
            :disable="service.result.page === service.result.pageCount || !service.hasMore"
            @click="service.loadPage(service.result.pageCount)"
          />
        </div>
      </div>
    </template>
  </q-table>
</template>
