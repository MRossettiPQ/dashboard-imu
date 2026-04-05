<script setup lang="ts">
import { computed, ref } from 'vue';
import { useProvidedSessionEditor } from 'src/composables/use-session';

type ViewMode = 'grid' | 'unified' | 'table' | 'summary';
interface Props {
  rightDrawer: boolean;
  actualStepName: 'first-step' | 'second-step' | 'save-step';
  actualStepLabel: string;
  viewType: ViewMode;
}

const { patient, session } = useProvidedSessionEditor();

// Lista de sensores requisitada
const requestedSensorList = ref(false);
const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:rightDrawer', val: boolean): void;
  (e: 'update:viewType', val: ViewMode): void;
}>();
const rightDrawer = computed({
  get: () => props.rightDrawer,
  set: (val: boolean) => emit('update:rightDrawer', val),
});
const actualView = computed({
  get: () => props.viewType ?? 'grid',
  set: (val: ViewMode) => emit('update:viewType', val),
});

const viewIcon = computed(() => {
  switch (actualView.value) {
    case 'grid':
      return 'grid_view';
    case 'unified':
      return 'layers';
    case 'table':
      return 'table_chart';
    case 'summary':
      return 'summarize';
    default:
      return 'view_module';
  }
});

async function requestSensorList(): Promise<void> {
  try {
    requestedSensorList.value = true;
    await Promise.all([]);
  } catch (e) {
    console.error(e);
  } finally {
    requestedSensorList.value = false;
  }
}
</script>

<template>
  <q-card flat bordered class="w-100 row navigation-header">
    <div class="row no-wrap column">
      <b class="f-bold f-medium">{{ actualStepLabel }}</b>
      <span v-if="actualStepLabel" class="f-bold f-medium">{{ patient?.user?.name }}</span>
    </div>

    <div class="row">
      <q-btn-group flat rounded>
        <q-btn
          class="row icon-primary"
          round
          dense
          unelevated
          :loading="requestedSensorList"
          :icon="requestedSensorList ? '' : 'refresh'"
          @click="requestSensorList"
        >
          <q-tooltip>Recarregar lista de sensores</q-tooltip>
        </q-btn>
        <q-btn
          class="row"
          round
          :color="session.sessionSensors.length > 0 ? 'positive' : 'negative'"
          dense
          :icon="session.sessionSensors.length > 0 ? 'sensors' : 'sensors_off'"
        >
          <q-tooltip>
            {{
              session.sessionSensors.length > 0
                ? 'Conectado em sensores'
                : 'Nenhum sensor conectado'
            }}
          </q-tooltip>
        </q-btn>

        <q-btn-dropdown
          v-if="actualStepName === 'second-step'"
          rounded
          unelevated
          size="md"
          class="row icon-primary"
          :icon="viewIcon"
          color="primary"
          no-caps
          label="View"
        >
          <q-list>
            <q-item clickable v-ripple @click="actualView = 'grid'">
              <q-item-section avatar><q-icon name="grid_view" /></q-item-section>
              <q-item-section>Grid</q-item-section>
            </q-item>
            <q-item clickable v-ripple @click="actualView = 'unified'">
              <q-item-section avatar><q-icon name="layers" /></q-item-section>
              <q-item-section>Unificado</q-item-section>
            </q-item>
            <q-item clickable v-ripple @click="actualView = 'table'">
              <q-item-section avatar><q-icon name="table_chart" /></q-item-section>
              <q-item-section>Tabela</q-item-section>
            </q-item>
            <q-item clickable v-ripple @click="actualView = 'summary'">
              <q-item-section avatar><q-icon name="summarize" /></q-item-section>
              <q-item-section>Sumário</q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn
          round
          dense
          unelevated
          size="md"
          class="row icon-primary"
          icon="settings"
          @click="rightDrawer = !rightDrawer"
        >
          <q-tooltip>Menu lateral direito</q-tooltip>
        </q-btn>
      </q-btn-group>
    </div>
  </q-card>
</template>

<style scoped lang="scss">
.navigation-header {
  padding: 6px;
  display: flex;
  justify-content: space-between;
}
</style>
