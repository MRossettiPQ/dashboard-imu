<template>
  <q-item clickable v-bind="$attrs" v-if="to" :to="to">
    <q-item-section v-if="icon" avatar>
      <q-icon :name="icon" />
    </q-item-section>

    <q-item-section>
      <q-item-label>{{ title }}</q-item-label>
      <q-item-label caption>{{ caption }}</q-item-label>
    </q-item-section>
  </q-item>

  <q-item clickable v-bind="$attrs" v-else :href="link" target="_blank">
    <q-item-section v-if="icon" avatar>
      <q-icon :name="icon" />
    </q-item-section>

    <q-item-section>
      <q-item-label>{{ title }}</q-item-label>
      <q-item-label caption>{{ caption }}</q-item-label>
    </q-item-section>
  </q-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export interface EssentialLinkProps {
  title: string;
  caption?: string;
  link?: string; // external URL
  icon?: string;
  to?: { name: string }; // Vue Router route object
  authenticated?: boolean;
  hiddenAuthenticated?: boolean;
}

export default defineComponent({
  name: 'EssentialLink',
  props: {
    title: {
      type: String,
      required: true,
    },
    caption: {
      type: String,
      default: '',
    },
    link: {
      type: String,
      default: '',
    },
    icon: {
      type: String,
      default: '',
    },
    to: {
      type: Object as () => { name: string },
      default: null,
    },
  },
});
</script>
