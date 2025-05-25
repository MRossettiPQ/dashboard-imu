<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer" />

        <q-toolbar-title> Quasar App </q-toolbar-title>

        <div>Quasar v{{ $q.version }}</div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <div class="u-h-100 column justify-between" style="max-height: 100vh">
        <q-list>
          <q-item-label header> Menu </q-item-label>

          <EssentialLink v-for="link in linksList" :key="link.title" v-bind="link" />
        </q-list>
        <essential-link title="Logout" icon="logout" />
      </div>
    </q-drawer>

    <q-page-container class="u-w-100 u-h-100">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import EssentialLink, { type EssentialLinkProps } from 'components/EssentialLink.vue';

const linksList: EssentialLinkProps[] = [
  {
    title: 'Perfil',
    caption: 'Ir para seu perfil',
    icon: 'person',
    to: { name: 'private.account' }, // ← Rota nomeada
  },
  {
    title: 'Pacientes',
    caption: 'Lista de pacientes',
    icon: 'people',
    to: { name: 'private.patient' }, // ← Outra rota nomeada
  },
];

export default defineComponent({
  name: 'MainLayout',

  components: {
    EssentialLink,
  },

  setup() {
    const leftDrawerOpen = ref(false);

    return {
      linksList,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
    };
  },
});
</script>
