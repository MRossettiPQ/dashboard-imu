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
    <q-header class="app-header" :elevated="false">
      <div class="app-header__accent" />
      <q-toolbar class="app-header__toolbar">
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          class="app-header__menu-btn"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title class="app-header__title">
          <span class="app-header__title-bold">Dashboard</span>
          <span class="app-header__title-light">IMU</span>
        </q-toolbar-title>

        <q-chip dense outline class="app-header__version" :label="'v' + $q.version" icon="info" />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered class="app-drawer">
      <div class="app-drawer__inner">
        <!-- Header -->
        <div class="app-drawer__header">
          <div class="app-drawer__logo">
            <q-icon name="dashboard" class="app-drawer__logo-icon" />
            <span class="app-drawer__logo-text">IMU</span>
          </div>
        </div>

        <q-separator class="app-drawer__separator" />

        <!-- Navigation -->
        <q-list class="app-drawer__nav">
          <p class="app-drawer__section-label">Navegação</p>
          <essential-link v-for="link in filteredLinkList" :key="link.title" v-bind="link" />
        </q-list>

        <q-space />

        <!-- Logout -->
        <div class="app-drawer__footer">
          <q-separator class="app-drawer__separator" />
          <essential-link v-if="store.isAuthenticated" title="Logout" icon="logout" />
        </div>
      </div>
    </q-drawer>

    <q-page-container class="u-w-100 u-h-100">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<style lang="scss" scoped>
.app-header {
  background: $primary;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);

  &__accent {
    height: 3px;
    background: linear-gradient(
      90deg,
      lighten($primary, 20%) 0%,
      $secondary 50%,
      lighten($primary, 20%) 100%
    );
  }

  &__toolbar {
    padding: 0 16px;
    min-height: 52px;
  }

  &__menu-btn {
    color: rgba(255, 255, 255, 0.9);
    transition: background 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.12);
    }
  }

  &__title {
    font-size: 18px;
    letter-spacing: 0.3px;
    user-select: none;
  }

  &__title-bold {
    font-weight: 700;
  }

  &__title-light {
    font-weight: 300;
    margin-left: 6px;
    opacity: 0.85;
  }

  &__version {
    color: rgba(255, 255, 255, 0.75) !important;
    border-color: rgba(255, 255, 255, 0.25) !important;
    font-size: 11px;
    font-weight: 500;
    letter-spacing: 0.3px;

    :deep(.q-icon) {
      color: rgba(255, 255, 255, 0.6);
      font-size: 14px;
    }
  }
}

.app-drawer {
  background: #fafbfc;
  border-right: 1px solid rgba(0, 0, 0, 0.06) !important;

  &__inner {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 8px 0;
  }

  &__header {
    padding: 20px 20px 16px;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__logo-icon {
    font-size: 28px;
    color: $primary;
    background: rgba($primary, 0.1);
    border-radius: 10px;
    padding: 8px;
  }

  &__logo-text {
    font-size: 20px;
    font-weight: 800;
    color: #1a1a2e;
    letter-spacing: 1.5px;
  }

  &__separator {
    margin: 4px 16px;
    opacity: 0.4;
  }

  &__nav {
    padding: 8px 0;
  }

  &__section-label {
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.8px;
    color: #9ca3af;
    padding: 8px 24px 4px;
    margin: 0;
  }

  &__footer {
    padding-bottom: 8px;
  }
}
</style>
