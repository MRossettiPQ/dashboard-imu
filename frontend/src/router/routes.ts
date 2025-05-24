import type { NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { useAuthStore } from 'stores/auth-store';
import { UserRole } from 'src/common/models/models';

const publicRoutes: RouteRecordRaw[] = [
  {
    name: 'public.login',
    path: 'login',
    component: () => import('pages/public/login/LoginPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
  {
    name: 'public.register',
    path: 'register',
    component: () => import('pages/public/register/RegisterPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
  {
    name: 'public.socket',
    path: 'socket',
    component: () => import('pages/public/socket/SocketPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
];
const privateRoutes: RouteRecordRaw[] = [
  {
    name: 'private.account',
    path: 'account',
    component: () => import('pages/private/account/AccountPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    name: 'private.patient',
    path: 'patient',
    component: () => import('pages/private/patient/PatientPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    name: 'private.session',
    path: 'session',
    component: () => import('pages/private/session/SessionPage.vue'),
    meta: {
      private: true,
    },
  },
];

const typeRoutes: RouteRecordRaw[] = [
  {
    name: 'shared',
    path: '',
    component: () => import('pages/shared/SharedApp.vue'),
    children: [
      {
        name: 'shared-home',
        path: '',
        component: () => import('pages/shared/home/HomePage.vue'),
        meta: {
          private: false,
        },
      },
      {
        name: 'public',
        path: '/',
        component: () => import('pages/public/PublicApp.vue'),
        children: publicRoutes,
        beforeEnter: (to, from, next) => {
          console.log('Before enter - publico');
          const store = useAuthStore();

          if (store.isAuthenticated && to.meta.hideAuthenticated) {
            return next({ name: 'shared' });
          }

          return next();
        },
      },
      {
        name: 'private',
        path: '/private',
        component: () => import('pages/private/PrivateApp.vue'),
        children: privateRoutes,
        beforeEnter: (to, from, next) => {
          console.log('Before enter - privados');
          const store = useAuthStore();
          const role = store.user?.role;
          if (role && ![UserRole.PHYSIOTHERAPIST, UserRole.ADMINISTRATOR].includes(role)) {
            return next();
          }

          return next({ name: 'public.login' });
        },
      },
    ],
  },
];

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('pages/MainApp.vue'),
    children: typeRoutes,
  },
  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/exceptions/not-found/NotFound.vue'),
  },
];

export const routeBeforeGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
): Promise<void> => {
  console.log('Before enter - geral');
  const store = useAuthStore();
  await store.isAuthenticatedOrLoadContext();

  console.log(store.isAuthenticated, to.meta.private);
  if (!store.isAuthenticated && to.meta.private) {
    return next({ name: 'public.login' });
  }
  next();
};
