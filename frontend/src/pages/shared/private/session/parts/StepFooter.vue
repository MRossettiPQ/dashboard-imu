<script setup lang="ts">
import { computed } from 'vue';
import type { SessionStore } from 'pages/shared/private/session/utils/SessionStore';

interface Props {
  session: SessionStore;
}

const props = defineProps<Props>();

const disablePrevButton = computed(() => {
  switch (props.session.actualStepValue) {
    case 'first-step':
      return true;
    case 'third-step':
      return props.session.inProgress;
    case 'second-step':
    default:
      return false;
  }
});
</script>

<template>
  <q-card flat bordered class="w-100 row navigation-footer">
    <q-btn class="row" dense round color="primary" icon="arrow_back_ios_new" />

    <q-btn-group v-if="session?.actualStepValue === 'third-step'" rounded flat>
      <q-btn
        dense
        color="primary"
        size="md"
        unelevated
        :disable="disablePrevButton"
        round
        icon="play_arrow"
        @click="session.start"
      />
      <q-btn dense color="primary" size="md" round icon="alarm" unelevated>
        <q-menu fit>
          <q-list style="min-width: 100px">
            <q-item>
              <!--              <q-form ref="mainForm" class="column form-lines form-lines__gap-sm" greedy>-->
              <!--                <q-checkbox label="Usar limitador" />-->
              <!--                <q-input-->
              <!--                  class="col"-->
              <!--                  :rules="[$rules.notBlank]"-->
              <!--                  filled-->
              <!--                  label="Tempo máximo"-->
              <!--                  suffix="segundos"-->
              <!--                />-->
              <!--              </q-form>-->
            </q-item>
            <q-item>
              <q-btn class="col" dense color="primary" size="md" unelevated icon="play_arrow" />
            </q-item>
          </q-list>
        </q-menu>
        <q-tooltip> Temporizador</q-tooltip>
      </q-btn>
      <q-btn dense color="primary" size="md" icon="stop" round unelevated />
      <q-btn dense color="primary" size="md" round icon="restart_alt" unelevated>
        <!--        <q-tooltip v-if="!syncedConnection.disableRestartBtn">-->
        <!--          Reiniciar medições, apaga as medições que não foram adicionadas ao movimento-->
        <!--        </q-tooltip>-->
      </q-btn>
      <q-btn dense color="primary" size="md" icon="done" round unelevated>
        <q-tooltip>Concluir movimento</q-tooltip>
      </q-btn>
    </q-btn-group>

    <q-btn class="row" dense round color="primary" @click="session.next" />
  </q-card>
</template>

<style scoped lang="scss">
.navigation-footer {
  padding: 6px;
  display: flex;
  justify-content: space-between;
}
</style>
