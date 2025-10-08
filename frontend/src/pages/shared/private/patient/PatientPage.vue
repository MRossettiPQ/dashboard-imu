<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { TableColumn } from 'src/common/models/models';
import { useRoute, useRouter } from 'vue-router';
import { sessionService } from 'src/common/services/session/session-service';
import { patientService } from 'src/common/services/patient/patient-service';
import { UserRole } from 'src/common/models/user/User';
import type { Patient } from 'src/common/models/patient/Patient';
import type { Session } from 'src/common/models/session/Session';
import type { QForm } from 'quasar';
import { formUtils } from 'src/common/utils/FormUtils';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const mainForm = ref<QForm | null>(null);
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
  cellphone: '',
  user: {
    username: '',
    name: '',
    email: '',
    password: '',
    role: UserRole.USER,
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

async function newSession() {
  await router.push({ name: 'private.session', params: { uuid: uuid.value } });
}

async function openSession(_evt: Event, row: Session, index: number) {
  await router.push({ name: 'private.session.view', params: { uuid: row.id, index } });
}

async function save() {
  try {
    saving.value = true;

    if (mainForm.value == null) {
      return;
    }

    await formUtils.validate(mainForm.value);
    const { data } = await patientService.save({ form: form.value });

    if (data?.content) {
      form.value = data?.content;
      await router.push({ name: 'private.patient', params: { uuid: data.content.id } });
    }
  } catch (error) {
    console.error(error);
  } finally {
    saving.value = false;
  }
}

async function search() {
  try {
    loading.value = true;
    await pagination.value.search();
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
      <q-card flat bordered class="col column u-w-100" style="min-width: 300px">
        <q-form ref="mainForm" class="col column u-w-100 u-gap-12 u-p-12 u-ph-16">
          <q-input
            v-model="form.cpf"
            dense
            label="CPF"
            outlined
            :rules="[$rules.notBlank]"
            unmasked-value
            mask="###.###.###-##"
          />
          <q-input
            v-model="form.phone"
            dense
            label="Telefone"
            outlined
            unmasked-value
            mask="(##) ####-####"
          />
          <q-input
            v-model="form.cellphone"
            dense
            label="Celular"
            outlined
            unmasked-value
            mask="(##) #####-####"
          />
          <q-input
            v-model="form.stature"
            dense
            type="number"
            label="Estatura"
            outlined
            maxlength="3"
            unmasked-value
            mask="###"
          />
          <q-input
            v-if="form.user && form.user?.id == null"
            v-model.trim="form.user.username"
            dense
            label="Username"
            outlined
            :rules="[$rules.notBlank]"
          />
          <q-input
            v-if="form.user && form.user?.id == null"
            v-model.trim="form.user.name"
            dense
            label="Nome"
            outlined
            :rules="[$rules.notBlank]"
          />
          <q-input
            v-if="form.user && form.user?.id == null"
            v-model.trim="form.user.email"
            dense
            type="email"
            label="E-mail"
            outlined
            :rules="[$rules.email]"
          />
          <q-input
            v-if="form.user && form.user?.id == null"
            v-model.trim="form.user.password"
            dense
            outlined
            type="password"
            label="Senha"
            :rules="[$rules.notBlank, $rules.minLength(8)]"
          />
          <q-btn
            color="primary"
            :disable="saving"
            :loading="saving"
            label="Salvar"
            size="16px"
            @click="save"
          />
        </q-form>
      </q-card>
      <custom-pagination
        v-if="uuid"
        class="col u-h-100 u-w-100"
        :service="pagination"
        :columns="columns"
        style="min-width: 300px; min-height: 480px"
        flat
        bordered
        :on-row-click="openSession"
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
              @click="newSession"
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
