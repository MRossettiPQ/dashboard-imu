<script setup lang="ts">
import { computed } from 'vue';
import type { Session } from 'src/common/models/session/Session';

interface Props {
  session: Session;
  rightDrawer: boolean;
  addSample: () => void;
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
        @click="addSample()"
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
