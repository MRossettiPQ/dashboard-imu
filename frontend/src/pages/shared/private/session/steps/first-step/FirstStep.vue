<script setup lang="ts">
import type { SessionState } from 'src/common/api/manual/constructors_api';

interface Props {
  session: SessionState;
}

const props = defineProps<Props>();
</script>

<template>
  <div class="flex column u-gap-12 u-h-min-0 u-w-min-0 u-w-100 u-h-100">
    {{ props }}
  </div>
</template>

<style scoped lang="scss">
.movement-table {
  min-height: 0;
  min-width: 0;
  height: 100%;
  width: 100%;

  thead tr th {
    position: sticky;
    z-index: 1;
  }

  thead tr:first-child th {
    top: 0;
  }
}

.img-div {
  min-width: 0;
  width: 100%;
  height: auto;
  min-height: 150px;
  max-width: 255px;
  border-radius: 8px;
}
</style>
