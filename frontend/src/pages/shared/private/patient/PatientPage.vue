<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import type { Result } from 'src/common/utils/PaginationUtils';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { TableColumn } from 'src/common/api/manual/models';
import { useRoute, useRouter } from 'vue-router';
import type { QForm } from 'quasar';
import { formUtils } from 'src/common/utils/FormUtils';
import LoadDiv from 'components/LoadDiv/LoadDiv.vue';
import { api } from 'boot/axios';
import type { PatientDto, SessionDto } from 'src/common/api/generated/models';
import { UserRole } from 'src/common/api/generated/models';
import type { AxiosResponse } from 'axios';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const loadingSearch = ref(false);
const saving = ref(false);
const mainForm = ref<QForm | null>(null);
const columns: TableColumn<SessionDto>[] = [
  {
    name: 'id',
    align: 'left',
    label: 'ID',
    field: 'id',
    sortable: true,
  },
];

type SessionParams = { page: number; rpp: number; term: string };
const pagination = ref<PaginationUtils<SessionDto, SessionParams>>(
  new PaginationUtils({
    service: () => {
      return new Promise((resolve) => {
        resolve({
          data: { content: null },
          status: 200,
        } as AxiosResponse<{ content: null }> as unknown as Result<SessionDto>);
      });
    },
    params: {
      page: 1,
      rpp: 10,
      term: '',
    },
  }),
);

const uuid = computed(() => route.params?.['uuid']?.toString());
const form = ref<PatientDto>({
  phone: null,
  cellphone: null,
  stature: null,
  cpf: null,
  birthday: null,
  user: {
    username: '',
    name: '',
    email: '',
    password: '',
    role: UserRole.USER,
  },
});

onMounted(async () => {
  try {
    loading.value = true;
    if (uuid.value) {
      pagination.value.service = (params) => api.getApiPatientsUuidSessions(uuid.value!, params);

      const [{ data }] = await Promise.all([
        api.getApiPatientsUuid(uuid.value),
        pagination.value.search(),
      ]);

      if (data.content) {
        form.value = data.content;
      }
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});

async function newSession() {
  await router.push({ name: 'private.session', params: { uuid: uuid.value } });
}

async function openSession(_evt: Event, row: SessionDto, index: number) {
  await router.push({ name: 'private.session.view', params: { uuid: row.id, index } });
}

async function save() {
  try {
    saving.value = true;

    if (mainForm.value == null) {
      return;
    }

    await formUtils.validate(mainForm.value);
    const { data } = await api.postApiPatients(form.value);

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
    loadingSearch.value = true;
    await pagination.value.search();
  } catch (error) {
    console.error(error);
  } finally {
    loadingSearch.value = false;
  }
}
</script>

<template>
  <custom-page id="patient-page">
    <load-div v-if="loading" />

    <div v-else class="patient-wrapper">
      <div class="patient-container">
        <!-- Page header -->
        <div class="patient-page-header">
          <div class="patient-page-header__left">
            <q-btn
              flat
              round
              dense
              icon="arrow_back"
              color="primary"
              class="patient-page-header__back"
              @click="router.push({ name: 'private.patients' })"
            />
            <q-icon name="person" class="patient-page-header__icon" />
            <div>
              <h2 class="patient-page-header__title">
                {{ uuid ? `Editar paciente - ${form.user?.name}` : 'Novo paciente' }}
              </h2>
              <p class="patient-page-header__subtitle">
                {{ uuid ? 'Atualize os dados do paciente' : 'Preencha os dados para cadastrar' }}
              </p>
            </div>
          </div>
        </div>

        <div class="patient-content">
          <!-- Form card -->
          <q-card class="patient-card" flat>
            <div class="patient-card__accent" />

            <div class="patient-card__body">
              <h3 class="patient-section-title">
                <q-icon name="badge" class="patient-section-title__icon" />
                Dados do paciente
              </h3>

              <q-form ref="mainForm" class="patient-form">
                <q-input
                  v-model="form.cpf"
                  label="CPF"
                  outlined
                  :rules="[$rules.notBlank]"
                  unmasked-value
                  mask="###.###.###-##"
                  class="patient-input"
                >
                  <template #prepend>
                    <q-icon name="fingerprint" color="primary" />
                  </template>
                </q-input>

                <q-input
                  v-model="form.phone"
                  label="Telefone"
                  outlined
                  unmasked-value
                  mask="(##) ####-####"
                  class="patient-input"
                >
                  <template #prepend>
                    <q-icon name="phone" color="primary" />
                  </template>
                </q-input>

                <q-input
                  v-model="form.cellphone"
                  label="Celular"
                  outlined
                  unmasked-value
                  mask="(##) #####-####"
                  class="patient-input"
                >
                  <template #prepend>
                    <q-icon name="smartphone" color="primary" />
                  </template>
                </q-input>

                <q-input
                  v-model="form.stature"
                  type="number"
                  label="Estatura (cm)"
                  outlined
                  maxlength="3"
                  unmasked-value
                  mask="###"
                  class="patient-input"
                >
                  <template #prepend>
                    <q-icon name="straighten" color="primary" />
                  </template>
                </q-input>

                <!-- User fields (only for new patients) -->
                <template v-if="form.user && form.user?.id == null">
                  <q-separator class="patient-separator" />

                  <h3 class="patient-section-title">
                    <q-icon name="person_add" class="patient-section-title__icon" />
                    Dados de acesso
                  </h3>

                  <q-input
                    v-model.trim="form.user!.username"
                    label="Username"
                    outlined
                    :rules="[$rules.notBlank]"
                    class="patient-input"
                  >
                    <template #prepend>
                      <q-icon name="alternate_email" color="primary" />
                    </template>
                  </q-input>

                  <q-input
                    v-model.trim="form.user!.name"
                    label="Nome"
                    outlined
                    :rules="[$rules.notBlank]"
                    class="patient-input"
                  >
                    <template #prepend>
                      <q-icon name="person_outline" color="primary" />
                    </template>
                  </q-input>

                  <q-input
                    v-model.trim="form.user!.email"
                    type="email"
                    label="E-mail"
                    outlined
                    :rules="[$rules.email]"
                    class="patient-input"
                  >
                    <template #prepend>
                      <q-icon name="mail_outline" color="primary" />
                    </template>
                  </q-input>

                  <q-input
                    v-model.trim="form.user!.password"
                    outlined
                    type="password"
                    label="Senha"
                    :rules="[$rules.notBlank, $rules.minLength(8)]"
                    class="patient-input"
                  >
                    <template #prepend>
                      <q-icon name="lock_outline" color="primary" />
                    </template>
                  </q-input>
                </template>

                <q-btn
                  color="primary"
                  :disable="saving"
                  :loading="saving"
                  label="Salvar"
                  size="16px"
                  unelevated
                  no-caps
                  class="patient-btn"
                  @click="save"
                />
              </q-form>
            </div>
          </q-card>

          <!-- Sessions card -->
          <q-card v-if="uuid" class="patient-card patient-card--sessions" flat>
            <div class="patient-card__accent" />

            <custom-pagination
              class="patient-sessions-table"
              :service="pagination"
              :columns="columns"
              :on-row-click="openSession"
              :loading="loadingSearch"
            >
              <template #top>
                <div class="patient-sessions-toolbar">
                  <q-btn
                    :disable="loading"
                    label="Nova sessão"
                    color="primary"
                    icon="add"
                    unelevated
                    no-caps
                    class="patient-sessions-toolbar__btn"
                    @click="newSession"
                  />
                  <q-input
                    v-model="pagination.params.term"
                    placeholder="Buscar sessão..."
                    borderless
                    class="patient-sessions-toolbar__search"
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
          </q-card>
        </div>
      </div>
    </div>
  </custom-page>
</template>

<style lang="scss" scoped>
.patient-wrapper {
  position: relative;
  width: 100%;
  min-height: 100%;
  height: fit-content;
  display: flex;
  flex-direction: column;
}

.patient-container {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 24px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ── Page header ── */
.patient-page-header {
  display: flex;
  align-items: center;

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__back {
    margin-right: -4px;
    transition: transform 0.15s ease;

    &:hover {
      transform: translateX(-2px);
    }
  }

  &__icon {
    font-size: 32px;
    color: $primary;
    background: rgba($primary, 0.08);
    border-radius: 12px;
    padding: 10px;
  }

  &__title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    letter-spacing: -0.3px;
  }

  &__subtitle {
    font-size: 13px;
    color: #6b7280;
    margin: 0;
  }
}

/* ── Content layout ── */
.patient-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ── Card ── */
.patient-card {
  border-radius: 16px !important;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.04),
    0 10px 24px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.03);
  background: #ffffff;
  overflow: hidden;
  flex: 1;
  min-width: 0;

  &__accent {
    height: 4px;
    background: linear-gradient(90deg, $primary, lighten($primary, 18%));
  }

  &__body {
    padding: 28px 28px 24px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &--sessions {
    min-width: 320px;
    min-height: 480px;
  }
}

/* ── Section title ── */
.patient-section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;

  &__icon {
    font-size: 20px;
    color: $primary;
  }
}

.patient-separator {
  margin: 4px 0;
  opacity: 0.5;
}

/* ── Form ── */
.patient-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.patient-input {
  :deep(.q-field__control) {
    border-radius: 10px;
  }

  :deep(.q-field--outlined .q-field__control:hover:before) {
    border-color: $primary;
  }

  :deep(.q-field--outlined.q-field--focused .q-field__control:after) {
    border-width: 2px;
  }
}

.patient-btn {
  width: 100%;
  border-radius: 10px !important;
  font-weight: 600;
  letter-spacing: 0.4px;
  padding: 10px 0 !important;
  margin-top: 4px;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba($primary, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

/* ── Sessions toolbar ── */
.patient-sessions-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 16px 20px 8px;
  flex-wrap: wrap;

  &__btn {
    border-radius: 10px !important;
    font-weight: 600;
    letter-spacing: 0.3px;
    padding: 8px 20px !important;
    transition:
      transform 0.15s ease,
      box-shadow 0.15s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 14px rgba($primary, 0.3);
    }
  }

  &__search {
    min-width: 180px;
    max-width: 280px;

    :deep(.q-field__control) {
      border-radius: 10px;
    }
  }
}

.patient-sessions-table {
  width: 100%;
}

/* ── Table styling overrides ── */
:deep(.q-table) {
  th {
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.4px;
    color: #6b7280;
    padding: 12px 16px;
  }

  td {
    padding: 14px 16px;
    font-size: 14px;
    color: #1a1a2e;
  }

  tbody tr {
    transition: background 0.15s ease;

    &:hover {
      background: rgba($primary, 0.03);
    }
  }
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .patient-content {
    flex-direction: column;
  }

  .patient-card--sessions {
    min-height: 360px;
  }

  .patient-container {
    padding: 20px 12px;
  }

  .patient-page-header__title {
    font-size: 18px;
  }

  .patient-sessions-toolbar {
    flex-direction: column;
    align-items: stretch;

    &__search {
      max-width: none;
    }
  }
}
</style>
