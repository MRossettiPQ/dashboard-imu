<script setup lang="ts">
import { computed } from 'vue';
import type { Session } from 'src/common/models/session/Session';
import type { Sensor } from 'src/common/models/sensor/Sensor';
import { socket } from 'boot/socket';
import { SocketEvents } from 'src/common/models/socket/SocketEvents';

interface Props {
  session: Session;
  selectedSensorList: Set<Sensor>;
  inProgress: boolean;
  loadingSave: boolean;
  actualStepName: 'first-step' | 'second-step' | 'third-step' | 'save-step';
}

const emit = defineEmits<{
  prev: [];
  next: [];
}>();
const props = defineProps<Props>();
const disablePrevButton = computed(() => {
  if (!props.session) return true;

  switch (props.actualStepName) {
    case 'first-step':
      return true;
    case 'third-step':
      return props.inProgress;
    case 'second-step':
    default:
      return false;
  }
});

const disableNextButton = computed(() => {
  if (!props.session) return true;

  switch (props.actualStepName) {
    case 'first-step':
      if (props.session.procedures.length < 1) return true;
      return props.session.procedures.some((p) => p.movements.length < 1);
    case 'second-step':
      return props.selectedSensorList.size < 1;
    case 'third-step':
      return false;
    // return this.syncedConnection?.blockSave || this.blockIfMovementsMeasurementsEmpty;
    default:
      return false;
  }
});

async function commandRestart() {
  console.log('commandRestart');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_RESTART, '');
  console.log(r);
}

async function commandStart() {
  console.log('commandStart');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_START, '');
  console.log(r);
}

async function commandStop() {
  console.log('commandStop');
  const r = await socket.emitWithAck(SocketEvents.CLIENT_SERVER_STOP, '');
  console.log(r);
}
</script>

<template>
  <q-card flat bordered class="w-100 row navigation-footer">
    <q-btn
      class="row"
      dense
      round
      :disable="disablePrevButton"
      color="primary"
      icon="arrow_back_ios_new"
      @click="emit('prev')"
    />

    <q-btn-group v-if="actualStepName === 'third-step'" rounded flat>
      <q-btn
        dense
        color="primary"
        size="md"
        unelevated
        round
        icon="play_arrow"
        @click="commandStart"
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
      <q-btn dense color="primary" size="md" icon="stop" round unelevated @click="commandStop" />
      <q-btn
        dense
        color="primary"
        size="md"
        round
        icon="restart_alt"
        unelevated
        @click="commandRestart"
      >
        <!--        <q-tooltip v-if="!syncedConnection.disableRestartBtn">-->
        <!--          Reiniciar medições, apaga as medições que não foram adicionadas ao movimento-->
        <!--        </q-tooltip>-->
      </q-btn>
      <q-btn dense color="primary" size="md" icon="done" round unelevated>
        <q-tooltip>Concluir movimento</q-tooltip>
      </q-btn>
    </q-btn-group>

    <q-btn
      class="row"
      dense
      round
      color="primary"
      :loading="loadingSave"
      :disable="disableNextButton"
      :icon="actualStepName !== 'third-step' ? 'arrow_forward_ios' : 'save'"
      @click="emit('next')"
    />
  </q-card>
</template>

<style scoped lang="scss">
.navigation-footer {
  padding: 6px;
  display: flex;
  justify-content: space-between;
}
</style>
