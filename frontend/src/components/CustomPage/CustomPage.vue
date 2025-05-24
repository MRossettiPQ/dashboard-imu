<template>
  <q-page class="page">
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

<style lang="scss" scoped>
.page {
  display: grid;
  grid-template-rows: min-content 1fr;
  grid-template-areas: 'header' 'page-content';
  max-height: calc(100vh - 50px);
  min-height: 0;
  height: 100%;
  padding: 12px 15px 0 15px;

  &__header {
    grid-area: header;
  }

  &__content {
    overflow: hidden;
    grid-area: page-content;
    height: 100%;
    display: grid;
    grid-template-rows: 1fr minmax(0, min-content);
    grid-template-areas: 'page-inner-content' 'page-actions';
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
  }

  &__inner-content {
    grid-area: page-inner-content;
    height: 100%;
  }

  &__actions {
    grid-area: page-actions;
  }
}
</style>
