<script setup lang="ts" generic="T, R extends { page: number; rpp: number }">
import type { QTableProps } from 'quasar';
import type PaginationUtils from 'src/common/utils/PaginationUtils';
import { computed, toRaw } from 'vue';
import type { TableColumn } from 'src/common/models/models';

// T = tipo dos dados por linha, ex: Record<string, any> ou algo mais específico
type TableProps = Omit<Partial<QTableProps>, 'columns'>;
interface Props<T, R extends { page: number; rpp: number }> extends TableProps {
  service: PaginationUtils<T, R>;
  editable?: boolean;
  rowKey?: string;
  columns: TableColumn<T>[];
  orderType?: 'ad' | 'da';
}

async function sortBy(): Promise<void> {
  console.log('sortBy', paginationInfo.value);
  await props.service.search();
}

const emit = defineEmits<(e: 'update:params', val: R) => void>();

const paginationInfo = computed({
  get: () => props.service.params,
  set: (val) => emit('update:params', val),
});
const props = defineProps<Props<T, R>>();
</script>

<template>
  <q-table
    :row-key="props.rowKey"
    :columns="props.columns"
    :dense="props.dense"
    :bordered="props.bordered"
    :flat="props.flat"
    :rows="props.service.items"
    :loading="props.service.loading"
    v-model:pagination="paginationInfo"
    :rows-per-page-options="[props.service.params.rpp]"
    :column-sort-order="props.orderType"
    binary-state-sort
    @request="sortBy"
    virtual-scroll
    no-data-label="Não encontrei nada para você"
    no-results-label="O filtro não encontrou nenhum resultado"
  >
    <slot />

    <template v-slot:top>
      <slot name="top" />
    </template>

    <template v-slot:body="props">
      <q-tr :props="props">
        <q-td v-for="col in props.cols" :key="col.name" :props="props">
          <div v-if="col.type == 'button'">
            <q-btn
              v-bind="col.props"
              @click="(event) => col.props.onClick?.(event, toRaw(props.row))"
              size="sm"
              round
              outline
              dense
            />
          </div>
          <template v-else>{{ col.value }}</template>
        </q-td>
      </q-tr>
    </template>

    <template v-slot:bottom>
      <div class="row u-w-100 items-center justify-between">
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
