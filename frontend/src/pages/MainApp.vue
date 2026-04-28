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
    const miniDrawer = ref(false);
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
      miniDrawer,
      leftDrawerOpen,
    };
  },
});
</script>

<template>
  <q-layout view="lHh lpR lfr">
    <q-header class="app-header" :elevated="false">
      <q-toolbar class="app-header__toolbar">
        <q-toolbar-title class="app-header__title">
          <span class="app-header__title-bold">Dashboard</span>
          <span class="app-header__title-light">IMU</span>
        </q-toolbar-title>

        <q-chip dense outline class="app-header__version" :label="'v' + $q.version" icon="info" />
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      :mini="miniDrawer"
      :mini-width="68"
      :width="260"
      class="app-drawer"
      :breakpoint="0"
    >
      <div class="app-drawer__inner">
        <!-- Header -->
        <div class="app-drawer__header">
          <div class="app-drawer__logo">
            <q-icon name="dashboard" class="app-drawer__logo-icon" />
            <transition name="fade">
              <span v-show="!miniDrawer" class="app-drawer__logo-text">IMU</span>
            </transition>
          </div>
          <q-btn
            v-show="!miniDrawer"
            flat
            dense
            round
            icon="chevron_left"
            class="app-drawer__toggle"
            @click="miniDrawer = true"
          />
        </div>

        <q-separator class="app-drawer__separator" />

        <!-- Expand button (mini mode) -->
        <div v-if="miniDrawer" class="app-drawer__expand">
          <q-btn
            flat
            dense
            round
            icon="chevron_right"
            class="app-drawer__toggle"
            @click="miniDrawer = false"
          />
        </div>

        <!-- Navigation -->
        <q-list class="app-drawer__nav">
          <transition name="fade">
            <p v-show="!miniDrawer" class="app-drawer__section-label">Navegação</p>
          </transition>
          <essential-link v-for="link in filteredLinkList" :key="link.title" v-bind="link" />
        </q-list>

        <q-space />

        <!-- Logout -->
        <div class="app-drawer__footer">
          <q-separator class="app-drawer__separator" />
          <essential-link
            v-if="store.isAuthenticated"
            title="Logout"
            icon="logout"
            @click="store.logOut"
          />
        </div>
      </div>
    </q-drawer>

    <q-page-container class="u-w-100 u-h-100" style="min-height: 0; min-width: 0">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<style lang="scss" scoped>
@use 'sass:color';
.app-header {
  background: $primary;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);

  &__accent {
    height: 3px;
    background: linear-gradient(
      90deg,
      color.adjust($primary, $lightness: 20%) 0%,
      $secondary 50%,
      color.adjust($primary, $lightness: 20%) 100%
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
    overflow: hidden;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 14px;
    min-height: 60px;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 10px;
    overflow: hidden;
    white-space: nowrap;
  }

  &__logo-icon {
    font-size: 26px;
    color: $primary;
    background: rgba($primary, 0.1);
    border-radius: 10px;
    padding: 8px;
    flex-shrink: 0;
  }

  &__logo-text {
    font-size: 20px;
    font-weight: 800;
    color: #1a1a2e;
    letter-spacing: 1.5px;
  }

  &__toggle {
    color: #6b7280;
    flex-shrink: 0;

    &:hover {
      color: $primary;
      background: rgba($primary, 0.08);
    }
  }

  &__expand {
    display: flex;
    justify-content: center;
    padding: 4px 0;
  }

  &__separator {
    margin: 4px 14px;
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
    white-space: nowrap;
    overflow: hidden;
  }

  &__footer {
    padding-bottom: 8px;
  }
}

/* Mini mode overrides via Quasar's mini class */
:deep(.q-drawer--mini) {
  .app-drawer__header {
    justify-content: center;
    padding: 16px 8px;
  }

  .app-drawer__separator {
    margin: 4px 8px;
  }

  .nav-link {
    margin: 4px 6px;
    padding: 10px;
    justify-content: center;

    .q-item__section--avatar {
      min-width: 0;
      padding-right: 0;
    }

    .q-item__section--main {
      display: none;
    }
  }
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
