<script setup lang="ts"></script>

<template>
  <q-table
    v-if="!!pagination"
    :bordered="bordered"
    :columns="columns"
    :flat="flat"
    :dense="dense"
    :row-key="rowKey"
    :rows="rows"
    :loading="pagination.loading"
    v-model:pagination="paginationInfo"
    :rows-per-page-options="[pagination.params.rpp]"
    :column-sort-order="orderType"
    binary-state-sort
    @request="sortBy"
    :title="title"
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
          <q-btn size="sm" round outline dense @click="editRowBtn(props.row)" icon="edit" />
        </q-td>
      </q-tr>
    </template>

    <template v-slot:bottom>
      <div class="row w-100 items-center justify-between">
        <div>Página {{ pagination.page }} de {{ pagination.pageCount }}</div>

        <div class="row">
          <q-btn
            v-if="pagination.page > 2"
            icon="first_page"
            color="grey-8"
            round
            dense
            flat
            :disable="pagination.page == 1"
            @click="setPage(1)"
          />

          <q-btn
            icon="chevron_left"
            color="grey-8"
            round
            dense
            flat
            :disable="pagination.page == 1"
            @click="loadPrev()"
          />

          <q-btn
            icon="chevron_right"
            color="grey-8"
            round
            dense
            flat
            :disable="pagination.page === pagination.pageCount || !pagination.hasMore"
            @click="loadNext()"
          />

          <q-btn
            v-if="pagination.pageCount > 2"
            icon="last_page"
            color="grey-8"
            round
            dense
            flat
            :disable="pagination.page === pagination.pageCount || !pagination.hasMore"
            @click="setPage(pagination.pageCount)"
          />
        </div>
      </div>
    </template>
  </q-table>
</template>

<style scoped></style>
