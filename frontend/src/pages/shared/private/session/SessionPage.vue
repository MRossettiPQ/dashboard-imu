<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, markRaw, ref } from 'vue';
import ErrorDiv from 'components/ErrorDiv/ErrorDiv.vue';
import LoadDiv from 'components/LoadDiv/LoadDiv.vue';
import StepHeader from 'pages/shared/private/session/parts/StepHeader.vue';
import StepFooter from 'pages/shared/private/session/parts/StepFooter.vue';
import DrawerMenu from 'pages/shared/private/session/parts/DrawerMenu.vue';
import FirstStep from 'pages/shared/private/session/steps/first-step/FirstStep.vue';
import SecondStep from 'pages/shared/private/session/steps/second-step/SecondStep.vue';
import ThirdStep from 'pages/shared/private/session/steps/third-step/ThirdStep.vue';
import { useSessionEditor, provideSessionEditor } from 'src/composables/use-session';

interface NavigationStep {
  order: number;
  name: 'first-step' | 'second-step' | 'save-step';
  label: string;
  value: unknown;
}

const sessionEditor = useSessionEditor();
provideSessionEditor(sessionEditor);
const { session, error, loading, inProgress, loadingSave } = sessionEditor;

// Menu lateral direito
const rightDrawer = ref(true);

// Movimentação de telas
const steps = ref<NavigationStep[]>([
  {
    order: 0,
    name: 'first-step',
    label: 'Selecionar procedimento',
    value: markRaw(FirstStep),
  },
  {
    order: 1,
    name: 'second-step',
    label: 'Ativar sensores',
    value: markRaw(SecondStep),
  },
  {
    order: 2,
    name: 'save-step',
    label: 'Salvar',
    value: markRaw(ThirdStep),
  },
]);

const selectedStep = ref<NavigationStep>(steps.value[0]!);
const actualStepOrder = computed(() => selectedStep.value.order);
const actualStepName = computed(() => selectedStep.value.name);
const actualStepValue = computed(() => selectedStep.value.value);
const actualStepLabel = computed(() => selectedStep.value.label);

function next() {
  if (!steps.value || steps.value.length === 0) {
    console.error('Steps não inicializados');
    return;
  }

  if (actualStepOrder.value < steps.value.length - 1) {
    const nextStep = steps.value.find(({ order }) => order === selectedStep.value.order + 1);
    if (nextStep) {
      selectedStep.value = nextStep;
    }
  } else {
    console.log('Não há próximo passo disponível');
  }
}

function prev(): void {
  if (!steps.value) return;

  const prevStep = steps.value.find(({ order }) => order === selectedStep.value.order - 1);
  if (prevStep) {
    selectedStep.value = prevStep;
  }
}

const viewType = ref<'grid' | 'unified' | 'table' | 'summary'>('grid');
</script>

<template>
  <custom-page>
    <error-div v-if="error" />
    <load-div v-else-if="loading" />
    <div v-else class="session-content">
      <step-header
        class="step-header"
        v-model:right-drawer="rightDrawer"
        v-model:view-type="viewType"
        :actual-step-name="actualStepName"
        :actual-step-label="actualStepLabel"
      />

      <q-card bordered flat class="step-content u-p-6">
        <Component
          :is="actualStepValue"
          v-model:right-drawer="rightDrawer"
          v-model:session="session"
          :view-type="viewType"
        />
        <drawer-menu v-model:right-drawer="rightDrawer" />
      </q-card>

      <step-footer
        class="step-footer"
        v-if="session"
        @next="next"
        @prev="prev"
        :in-progress="inProgress"
        :loading-save="loadingSave"
        :actual-step-name="actualStepName"
      />
    </div>
  </custom-page>
</template>

<style scoped lang="scss">
.session-content {
  height: 100%;
  width: 100%;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;

  .step-header {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }

  .step-content {
    flex: 1 1 0;
    height: 100%;
    width: 100%;
    min-height: 0;
    min-width: 0;
    display: flex;
    flex-direction: row;
  }

  .step-footer {
    flex: 0 0 0;
    height: 100%;
    width: 100%;
  }
}
</style>
