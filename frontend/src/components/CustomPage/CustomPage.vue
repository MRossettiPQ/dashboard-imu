<template>
  <q-page class="page main-content">
    <!--    <div v-if="loading" class="div-loading">-->
    <!--      <q-inner-loading color="primary" showing size="50px" />-->
    <!--    </div>-->
    <slot class="page__header" name="header" />

    <div class="page__content bordered">
      <div class="page__inner-content scroll">
        <slot />
      </div>
      <slot class="page__actions" name="actions" />
    </div>
  </q-page>
</template>
<script setup lang="ts"></script>

<style scoped></style>
