<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import type { AnimationItem } from 'lottie-web';
import lottie from 'lottie-web';

const container = ref<HTMLElement | null>(null);
let anim: AnimationItem | null = null;

onMounted(() => {
  anim = lottie.loadAnimation({
    container: container.value!,
    renderer: 'svg',
    loop: true,
    autoplay: true,
    // Substitua pelo seu JSON
    path: '/lotties/loading.json',
  });
});

onBeforeUnmount(() => {
  if (anim) {
    anim.destroy();
  }
});
</script>

<template>
  <div class="flex items-center justify-center u-h-min-0 u-w-min-0 u-h-100 u-w-100">
    <div ref="container" style="width: 150px; height: 150px"></div>
  </div>
</template>

<style scoped></style>
