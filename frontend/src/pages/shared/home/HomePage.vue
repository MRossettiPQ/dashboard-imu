<script setup lang="ts">
import { onMounted, ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { api } from 'boot/axios';
import type { SensorInfoRead } from 'src/common/api/generated/models';

const loading = ref(false);

type SensorParams = { page: number; rpp: number; term: string };
const pagination = ref<PaginationUtils<SensorInfoRead, SensorParams>>(
  new PaginationUtils({
    service: api.getApiSessionsSensorsAvailable,
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
    <div class="sensors-wrapper">
      <div class="page-header">
        <q-icon name="sensors" class="page-header__icon" />
        <h2 class="page-header__title">Sensores Disponíveis</h2>
        <p class="page-header__subtitle">Gerencie os dispositivos IMU conectados na rede</p>
      </div>

      <div v-if="loading" class="flex flex-center q-pa-xl">
        <q-spinner-dots color="primary" size="3em" />
      </div>

      <div v-else-if="!pagination.items || pagination.items.length === 0" class="empty-state">
        <q-icon name="wifi_off" size="48px" color="grey-4" />
        <p>Nenhum sensor encontrado no momento.</p>
        <q-btn outline color="primary" label="Recarregar" @click="search" no-caps />
      </div>

      <div v-else class="row q-col-gutter-lg q-pb-xl">
        <div
          v-for="sensor in pagination.items"
          :key="String(sensor.id)"
          class="col-12 col-sm-6 col-md-4 col-lg-3"
        >
          <q-card class="sensor-card" flat>
            <div class="sensor-card__accent" />

            <div class="sensor-card__content">
              <div class="sensor-header">
                <div class="sensor-header__title-wrapper">
                  <q-icon name="memory" class="sensor-header__icon" />
                  <h3 class="sensor-header__title">Sensor {{ sensor.id || 'N/A' }}</h3>
                </div>
                <q-badge rounded color="positive" class="sensor-header__badge"> Online </q-badge>
              </div>

              <div class="sensor-info">
                <div class="sensor-info__item" v-if="sensor.macAddress">
                  <q-icon name="router" size="18px" color="primary" />
                  <span>{{ sensor.macAddress }}</span>
                </div>

                <div class="sensor-info__item" v-if="sensor.sensorName">
                  <q-icon name="info_outline" size="18px" color="primary" />
                  <span class="text-ellipsis">{{ sensor.sensorName }}</span>
                </div>
              </div>

              <q-separator color="grey-2" />

              <div class="sensor-footer">
                <q-btn
                  outline
                  color="primary"
                  label="Ver Detalhes"
                  size="13px"
                  class="sensor-btn"
                  unelevated
                  no-caps
                />
              </div>
            </div>
          </q-card>
        </div>
      </div>
    </div>
  </custom-page>
</template>

<style scoped lang="scss">
.sensors-wrapper {
  padding: 24px 32px;
  min-height: 0;
  width: 100%;
}

/* ── Cabeçalho da Página (Estilo baseado no Login) ── */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 8px;

  &__icon {
    font-size: 32px;
    color: $primary;
    background: rgba($primary, 0.08);
    border-radius: 50%;
    padding: 12px;
    margin-bottom: 8px;
  }

  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    letter-spacing: -0.3px;
  }

  &__subtitle {
    font-size: 15px;
    color: #6b7280;
    margin: 0;
    font-weight: 400;
  }
}

/* ── Cards dos Sensores ── */
.sensor-card {
  position: relative;
  z-index: 1;
  height: 100%;
  border-radius: 16px !important;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.04),
    0 10px 24px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.03);
  background: #ffffff;
  overflow: hidden;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-3px);
    box-shadow:
      0 8px 12px rgba(0, 0, 0, 0.05),
      0 16px 32px rgba(0, 0, 0, 0.1),
      0 0 0 1px rgba(0, 0, 0, 0.03);
  }

  &__accent {
    height: 5px;
    background: linear-gradient(90deg, $primary, lighten($primary, 18%));
  }

  &__content {
    padding: 24px;
    display: flex;
    flex-direction: column;
    flex: 1;
    gap: 20px;
  }
}

.sensor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  &__title-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__icon {
    font-size: 24px;
    color: $primary;
    background: rgba($primary, 0.08);
    border-radius: 8px;
    padding: 6px;
  }

  &__title {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
  }

  &__badge {
    padding: 4px 8px;
    font-weight: 600;
    letter-spacing: 0.3px;
  }
}

.sensor-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1; /* Empurra o footer pra baixo se o card crescer */

  &__item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    font-size: 14px;
    color: #4b5563;
    line-height: 1.4;

    span {
      margin-top: 1px;
    }
  }
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.observation-text {
  display: -webkit-box;
  -webkit-line-clamp: 2; /* Limita a 2 linhas para não quebrar o layout */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sensor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
}

.sensor-stat {
  display: flex;
  flex-direction: column;

  &__value {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a2e;
    line-height: 1;
  }

  &__label {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 500;
    margin-top: 4px;
  }
}

.sensor-btn {
  border-radius: 8px !important;
  font-weight: 600;
  padding: 4px 16px !important;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;

  &:hover {
    background-color: rgba($primary, 0.05);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  color: #6b7280;
  text-align: center;
  gap: 16px;

  p {
    font-size: 16px;
    margin: 0;
  }
}

@media (max-width: 600px) {
  .sensors-wrapper {
    padding: 16px;
  }

  .page-header {
    align-items: center;
    text-align: center;

    &__title {
      font-size: 22px;
    }
  }
}
</style>
