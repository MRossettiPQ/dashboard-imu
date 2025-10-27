<script setup lang="ts">
import type { SessionStore } from 'pages/shared/private/session/utils/SessionStore';
import { computed } from 'vue';

interface Props {
  session: SessionStore;
  rightDrawer: boolean;
}
const props = defineProps<Props>();
const emit = defineEmits<(e: 'update:rightDrawer', val: boolean) => void>();

const rightDrawer = computed({
  get: () => props.rightDrawer,
  set: (val) => emit('update:rightDrawer', val),
});
const showActualMovement = computed(() => props.session.actualStepName == 'third-step');
</script>

<template>
  <q-card flat bordered class="w-100 row navigation-header">
    <p>Step header</p>
    <div class="row no-wrap">
      <span class="f-bold f-medium">{{ session.actualStepLabel }}</span>
      <span v-if="showActualMovement" class="f-bold f-medium">
        : {{ session.actualMovementLabel }}
      </span>
    </div>

    <q-btn
      v-if="session.actualStepOrder === 0"
      round
      dense
      unelevated
      size="md"
      class="row icon-primary"
      icon="settings"
      @click="rightDrawer = !rightDrawer"
    />
  </q-card>
</template>

<style scoped lang="scss">
.navigation-header {
  padding: 6px;
  display: flex;
  justify-content: space-between;
}
</style>
