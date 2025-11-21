<script lang="ts">
import { computed, defineComponent, ref } from 'vue';
import EssentialLink, { type EssentialLinkProps } from 'components/EssentialLink.vue';
import { useAuthStore } from 'stores/auth-store';

const linksList: EssentialLinkProps[] = [
  {
    title: 'Home',
    caption: 'Ir para home',
    icon: 'home',
    to: { name: 'shared.home' }, // ← Rota nomeada
    authenticated: false,
  },
  {
    title: 'Login',
    caption: 'Ir para o login',
    icon: 'login',
    to: { name: 'public.login' }, // ← Rota nomeada
    authenticated: false,
  },
  {
    title: 'Cadastro',
    caption: 'Ir para o cadastro',
    icon: 'person',
    to: { name: 'public.register' }, // ← Rota nomeada
    authenticated: false,
  },
  {
    title: 'Perfil',
    caption: 'Ir para seu perfil',
    icon: 'person',
    to: { name: 'private.account' }, // ← Rota nomeada
    authenticated: true,
  },
  {
    title: 'Pacientes',
    caption: 'Lista de pacientes',
    icon: 'people',
    to: { name: 'private.patients' }, // ← Outra rota nomeada
    authenticated: true,
  },
];

export default defineComponent({
  name: 'MainLayout',

  components: {
    EssentialLink,
  },

  setup() {
    const leftDrawerOpen = ref(false);
    const store = useAuthStore();

    const filteredLinkList = computed(() => {
      return linksList.filter((link) => {
        if (link.authenticated === true) {
          return store.isAuthenticated;
        }
        if (link.authenticated === false) {
          return !store.isAuthenticated;
        }
        return true;
      });
    });

    return {
      store,
      filteredLinkList,
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
    };
  },
});
</script>

<template>
  <q-layout view="lHh lpR lfr">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer" />

        <q-toolbar-title> Dashboard IMU</q-toolbar-title>

        <div>Quasar v{{ $q.version }}</div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <div class="u-h-100 column justify-between" style="max-height: 100vh">
        <q-list>
          <q-item-label header> Menu</q-item-label>

          <essential-link v-for="link in filteredLinkList" :key="link.title" v-bind="link" />
        </q-list>
        <essential-link v-if="store.isAuthenticated" title="Logout" icon="logout" />
      </div>
    </q-drawer>

    <q-page-container class="u-w-100 u-h-100">
      <router-view />
    </q-page-container>
  </q-layout>
</template>
