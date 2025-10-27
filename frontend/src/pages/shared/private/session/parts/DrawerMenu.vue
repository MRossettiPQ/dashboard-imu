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
</script>

<template>
  <q-drawer
    ref="menuRef"
    bordered
    :value="rightDrawer"
    side="right"
    @hide="rightDrawer = false"
    content-class="bg-grey-1 column justify-between no-wrap"
  >
    <div class="p-12 column gap-12 overflow-auto no-wrap">
      <q-btn
        dense
        outline
        class="row icon-primary"
        label="Medições"
        icon="add"
        @click="session.addSample()"
      />
      <p>Procedimentos</p>
      <div v-for="(procedure, index) in session.procedures" :key="index">
        <p>Procedimento {{ procedure.type }}</p>
        <p>Movimentos</p>
        <div v-for="(movement, index) in procedure.movements" :key="index">
          <p>Movimento {{ movement.type }}</p>
        </div>
      </div>
    </div>
  </q-drawer>
</template>

<style scoped lang="scss"></style>
