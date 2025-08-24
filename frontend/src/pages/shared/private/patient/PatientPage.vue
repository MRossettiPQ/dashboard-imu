<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { TableColumn } from 'src/common/models/models';
import { useRoute } from 'vue-router';
import { sessionService } from 'src/common/services/session/session-service';
import { patientService } from 'src/common/services/patient/patient-service';
import dayjs from 'dayjs';
import CustomDate from 'components/CustomDate/CustomDate.vue';
import { UserRole } from 'src/common/models/user/User';
import type { Patient } from 'src/common/models/patient/Patient';
import type { Session } from 'src/common/models/session/Session';

const route = useRoute();
const loading = ref(false);
const columns: TableColumn<Session>[] = [
  {
    name: 'id',
    align: 'left',
    label: 'ID',
    field: 'id',
    sortable: true,
  },
];
const pagination = ref(
  new PaginationUtils({
    service: sessionService.list,
    params: {
      page: 1,
      rpp: 10,
      term: '',
    },
  }),
);

const uuid = computed(() => route.params?.['uuid']?.toString());
const form = ref<Patient>({
  phone: '',
  birthday: dayjs(),
  user: {
    username: '',
    name: '',
    email: '',
    password: '',
    role: UserRole.PATIENT,
  },
});

onMounted(async () => {
  if (uuid.value) {
    const { data } = await patientService.get({ uuid: uuid.value });
    if (data.content) {
      form.value = data.content;
    }
  }
});

function open() {
  console.log('open');
  console.log(uuid.value);
}

function search() {
  try {
    loading.value = true;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <custom-page id="patient-page">
    <div class="u-w-100 u-h-100 flex u-gap-18 u-pb-12">
      <q-card
        flat
        bordered
        class="col column u-w-100 u-gap-12 u-p-12 u-ph-16"
        style="min-width: 300px"
      >
        <q-input v-model="form.cpf" dense label="CPF" outlined :rules="[$rules.notBlank]" />
        <q-input v-model="form.phone" dense label="Telefone" outlined :rules="[$rules.notBlank]" />
        <q-input
          v-model="form.stature"
          dense
          type="number"
          label="Estatura"
          outlined
          :rules="[$rules.notBlank]"
        />
        <custom-date
          label="Data de nascimento"
          v-model="form.birthday"
          type="date"
          outlined
          dense
          :rules="[$rules.notBlank]"
        >
          <div class="row items-center justify-end">
            <q-btn v-close-popup label="Fechar" color="primary" flat />
          </div>
        </custom-date>
        <q-input
          v-if="form.user"
          v-model.trim="form.user.username"
          dense
          label="Username"
          outlined
          :rules="[$rules.notBlank]"
        />
        <q-input
          v-if="form.user"
          v-model.trim="form.user.name"
          dense
          label="Nome"
          outlined
          :rules="[$rules.notBlank]"
        />
        <q-input
          v-if="form.user"
          v-model.trim="form.user.email"
          dense
          type="email"
          label="E-mail"
          outlined
          :rules="[$rules.email]"
        />
        <q-input
          v-if="form.user"
          v-model.trim="form.user.password"
          dense
          outlined
          type="password"
          label="Senha"
          :rules="[$rules.notBlank, $rules.minLength(8)]"
        />
      </q-card>
      <custom-pagination
        class="col u-h-100 u-w-100"
        :service="pagination"
        :columns="columns"
        style="min-width: 300px; min-height: 480px"
        flat
        bordered
      >
        <template #top>
          <div class="flex justify-between u-gap-8 u-w-100 no-wrap">
            <q-btn
              :disable="loading"
              :label="$q.screen.sm ? 'Sessão' : ''"
              color="primary"
              dense
              icon="add"
              unelevated
              outline
              @click="open"
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
    </div>
  </custom-page>
</template>

<style scoped lang="scss"></style>
