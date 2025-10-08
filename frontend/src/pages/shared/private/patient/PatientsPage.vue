<script setup lang="ts">
import { onMounted, ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import { patientService } from 'src/common/services/patient/patient-service';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { TableColumn } from 'src/common/models/models';
import { useRouter } from 'vue-router';
import type { Patient } from 'src/common/models/patient/Patient';
import type { User } from 'src/common/models/user/User';

const router = useRouter();
const loading = ref(false);
const columns: TableColumn<Patient>[] = [
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
    format(val: User /* row: Patient */) {
      return val.name;
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
      onClick: (evt: Event, row?: Patient) => {
        void open(evt, row);
      },
    },
  },
];
const pagination = ref(
  new PaginationUtils({
    service: patientService.list,
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

async function open(_evt: Event, row?: Patient) {
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
