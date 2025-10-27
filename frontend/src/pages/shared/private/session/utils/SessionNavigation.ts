import FirstStep from 'pages/shared/private/session/steps/first-step/FirstStep.vue';
import SecondStep from 'pages/shared/private/session/steps/second-step/SecondStep.vue';
import ThirdStep from 'pages/shared/private/session/steps/third-step/ThirdStep.vue';
import { Session } from 'src/common/models/session/Session';
import { markRaw } from 'vue';

interface NavigationStep {
  order: number;
  name: 'first-step' | 'second-step' | 'third-step';
  label: string;
  value: unknown;
}

class SessionNavigation extends Session {
  started = false;
  selectedStep: NavigationStep;
  steps: NavigationStep[];

  constructor() {
    super();

    // Inicialize steps primeiro
    this.steps = [
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
        name: 'third-step',
        label: 'Captar medições',
        value: markRaw(ThirdStep),
      },
    ];

    // Depois inicialize selectedStep
    this.selectedStep = this.steps[0]!;
    this.started = false;

    // Bind dos métodos
    this.next = this.next.bind(this);
    this.prev = this.prev.bind(this);
    this.nextStep = this.nextStep.bind(this);
  }

  get actualStep(): NavigationStep {
    return this.selectedStep;
  }

  get actualStepOrder(): number {
    return this.selectedStep?.order ?? 0;
  }

  get actualStepName(): string {
    return this.selectedStep?.name ?? 'first-step';
  }

  get actualStepValue(): unknown {
    return this.selectedStep?.value;
  }

  get actualStepLabel(): string {
    return this.selectedStep?.label ?? '';
  }

  next() {
    // Verificação de segurança
    if (!this.steps || this.steps.length === 0) {
      console.error('Steps não inicializados');
      return;
    }

    if (this.actualStepOrder < this.steps.length - 1) {
      this.nextStep();
    } else {
      console.log('Não há próximo passo disponível');
    }
  }

  nextStep() {
    if (!this.steps) return;

    const nextStep = this.steps.find(({ order }) => order === this.selectedStep.order + 1);
    if (nextStep) {
      this.selectedStep = nextStep;
    }
  }

  prev() {
    if (!this.steps) return;

    const prevStep = this.steps.find(({ order }) => order === this.selectedStep.order - 1);
    if (prevStep) {
      this.selectedStep = prevStep;
    }
  }

  canGoNext(): boolean {
    return this.steps && this.actualStepOrder < this.steps.length - 1;
  }

  canGoPrev(): boolean {
    return this.steps && this.actualStepOrder > 0;
  }

  goToStep(stepOrder: number) {
    if (!this.steps) return;

    const targetStep = this.steps.find(({ order }) => order === stepOrder);
    if (targetStep) {
      this.selectedStep = targetStep;
    }
  }
}

export { SessionNavigation, type NavigationStep };
