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
    <div class="patients-wrapper">
      <div class="patients-container">
        <!-- Page header -->
        <div class="patients-page-header">
          <div class="patients-page-header__left">
            <q-icon name="groups" class="patients-page-header__icon" />
            <div>
              <h2 class="patients-page-header__title">Pacientes</h2>
              <p class="patients-page-header__subtitle">Gerencie os pacientes cadastrados</p>
            </div>
          </div>
        </div>

        <!-- Table card -->
        <q-card class="patients-card" flat>
          <div class="patients-card__accent" />

          <custom-pagination class="patients-table" :service="pagination" :columns="columns">
            <template #top>
              <div class="patients-toolbar">
                <q-btn
                  :disable="loading"
                  label="Novo paciente"
                  color="primary"
                  icon="add"
                  unelevated
                  no-caps
                  class="patients-toolbar__btn"
                  @click="newPatient"
                />
                <q-input
                  v-model="pagination.params.term"
                  placeholder="Buscar paciente..."
                  borderless
                  class="patients-toolbar__search"
                  color="primary"
                  debounce="300"
                  dense
                  outlined
                >
                  <template #prepend>
                    <q-icon name="search" color="grey-6" />
                  </template>
                </q-input>
              </div>
            </template>
          </custom-pagination>
        </q-card>
      </div>
    </div>
  </custom-page>
</template>

<style lang="scss" scoped>
.patients-wrapper {
  position: relative;
  width: 100%;
  min-height: 100%;
  height: fit-content;
  display: flex;
  flex-direction: column;
}

.patients-container {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 24px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ── Page header ── */
.patients-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
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

/* ── Card ── */
.patients-card {
  height: 100%;
  border-radius: 16px !important;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.04),
    0 10px 24px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.03);
  background: #ffffff;
  overflow: hidden;

  &__accent {
    height: 4px;
    background: linear-gradient(90deg, $primary, lighten($primary, 18%));
  }
}

.patients-table {
  width: 100%;
  min-height: 400px;
  height: 100%;
}

/* ── Toolbar ── */
.patients-toolbar {
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
    min-width: 220px;
    max-width: 320px;

    :deep(.q-field__control) {
      border-radius: 10px;
    }
  }
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

@media (max-width: 600px) {
  .patients-container {
    padding: 20px 12px;
  }

  .patients-toolbar {
    flex-direction: column;
    align-items: stretch;

    &__search {
      max-width: none;
    }
  }

  .patients-page-header__title {
    font-size: 18px;
  }
}
</style>
