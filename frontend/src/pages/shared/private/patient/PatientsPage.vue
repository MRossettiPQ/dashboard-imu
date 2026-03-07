<script setup lang="ts">
import { onMounted, ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { TableColumn } from 'src/common/api/manual/models';
import { useRouter } from 'vue-router';
import { api } from 'boot/axios';
import type { PatientDto, PatientDtoUser } from 'src/common/api/generated/models';

const router = useRouter();
const loading = ref(false);
const columns: TableColumn<PatientDto>[] = [
  {
    name: 'id',
    align: 'left',
    label: 'ID',
    field: 'id',
    sortable: true,
  },
  {
    name: 'name',
    align: 'left',
    label: 'Nome',
    format(val: PatientDtoUser | undefined) {
      return val?.name ?? '';
    },
    field: 'user',
  },
  {
    name: 'cpf',
    align: 'left',
    label: 'CPF',
    field: 'cpf',
  },
  {
    label: '#',
    name: 'menu',
    field: 'menu',
    type: 'button',
    align: 'left',
    props: {
      color: 'primary',
      icon: 'edit',
      onClick: (evt: Event, row?: PatientDto) => {
        void open(evt, row);
      },
    },
  },
];

type PatientParams = { page: number; rpp: number; term: string };
const pagination = ref<PaginationUtils<PatientDto, PatientParams>>(
  new PaginationUtils({
    service: api.getApiPatients,
    params: {
      page: 1,
      rpp: 10,
      term: '',
    },
  }),
);

onMounted(async () => {
  await search();
});

async function open(_evt: Event, row?: PatientDto) {
  await router.push({ name: 'private.patient', params: { uuid: row?.id } });
}
async function newPatient() {
  await router.push({ name: 'private.patient.new' });
}

async function search() {
  try {
    loading.value = true;
    await pagination.value.search();
    console.log(pagination.value.items);
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <custom-page>
    <custom-pagination class="u-h-100 u-w-100" :service="pagination" :columns="columns">
      <template #top>
        <div class="flex justify-between u-gap-8 u-w-100 no-wrap">
          <q-btn
            :disable="loading"
            :label="$q.screen.sm ? 'Paciente' : ''"
            color="primary"
            dense
            icon="add"
            unelevated
            outline
            @click="newPatient"
          />
          <q-input
            v-model="pagination.params.term"
            borderless
            class="col col-sm-5"
            color="primary"
            debounce="300"
            dense
            outlined
          >
            <template #append>
              <q-icon name="search" @click="search" />
            </template>
          </q-input>
        </div>
      </template>
    </custom-pagination>
  </custom-page>
</template>
