<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  rightDrawer: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'update:rightDrawer', val: boolean): void;
}>();

const rightDrawer = computed({
  get: () => props.rightDrawer,
  set: (val) => emit('update:rightDrawer', val),
});
</script>

<template>
  <q-drawer
    bordered
    side="right"
    :modelValue="rightDrawer"
    class="custom-drawer"
    content-class="bg-grey-1 column justify-between no-wrap u-h-100"
  >
    <q-card class="u-p-12 column u-gap-12 no-wrap u-h-100">
      <q-btn
        rounded
        dense
        unelevated
        size="md"
        label="Add medições"
        class="row icon-primary"
        icon="settings"
      />

      <q-separator />
    </q-card>
  </q-drawer>
</template>

<style scoped lang="scss">
.q-drawer {
  top: 0 !important;
}
</style>
