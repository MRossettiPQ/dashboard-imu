<script setup lang="ts">
import type { SessionStore } from 'pages/shared/private/session/utils/SessionStore';

interface Props {
  session: SessionStore;
}

defineProps<Props>();
</script>

<template>
  <div>Second step</div>
</template>

<style scoped lang="scss"></style>
