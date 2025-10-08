import type { NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { useAuthStore } from 'stores/auth-store';
import { UserRole } from 'src/common/models/user/User';

const publicRoutes: RouteRecordRaw[] = [
  {
    path: 'login',
    name: 'public.login',
    component: () => import('pages/shared/public/login/LoginPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
  {
    path: 'register',
    name: 'public.register',
    component: () => import('pages/shared/public/register/RegisterPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
  {
    path: 'socket',
    name: 'public.socket',
    component: () => import('pages/shared/public/socket/SocketPage.vue'),
    meta: {
      hideAuthenticated: true,
      private: false,
    },
  },
];
const privateRoutes: RouteRecordRaw[] = [
  {
    path: 'account',
    name: 'private.account',
    component: () => import('pages/shared/private/account/AccountPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    path: 'patient',
    name: 'private.patients',
    component: () => import('pages/shared/private/patient/PatientsPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    path: 'patient/new',
    name: 'private.patient.new',
    component: () => import('pages/shared/private/patient/PatientPage.vue'),
    meta: { private: true },
  },
  {
    path: 'patient/:uuid([0-9a-fA-F-]{36})',
    name: 'private.patient',
    component: () => import('pages/shared/private/patient/PatientPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    path: 'session/:uuid([0-9a-fA-F-]{36})',
    name: 'private.session',
    component: () => import('pages/shared/private/session/SessionPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    path: 'session/view/:uuid([0-9a-fA-F-]{36})',
    name: 'private.session.view',
    component: () => import('pages/shared/private/session/SessionPage.vue'),
    meta: {
      private: true,
    },
  },
];

const typeRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'shared',
    component: () => import('pages/shared/SharedApp.vue'),
    children: [
      {
        path: '',
        name: 'public',
        component: () => import('pages/shared/public/PublicApp.vue'),
        children: publicRoutes,
        beforeEnter: (to, _from, next) => {
          console.log('Before enter - publico ->', to.fullPath);
          const store = useAuthStore();

          if (store.isAuthenticated && to.meta.hideAuthenticated) {
            return next({ name: 'shared.home' });
          }

          return next();
        },
      },
      {
        path: '/private',
        name: 'private',
        component: () => import('pages/shared/private/PrivateApp.vue'),
        children: privateRoutes,
        beforeEnter: (to, _from, next) => {
          console.log('Before enter - privados ->', to.fullPath);
          const store = useAuthStore();
          const role = store.user?.role;

          if (role && [UserRole.PHYSIOTHERAPIST, UserRole.ADMINISTRATOR].includes(role)) {
            return next();
          }

          return next({ name: 'public.login' });
        },
      },
      {
        path: '/home',
        name: 'shared.home',
        component: () => import('pages/shared/home/HomePage.vue'),
        meta: {
          private: false,
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
  console.log('Before enter - geral ->', to.fullPath);
  const store = useAuthStore();
  await store.isAuthenticatedOrLoadContext();

  // console.log(store.isAuthenticated, to.meta.private);
  if (!store.isAuthenticated && to.meta.private) {
    return next({ name: 'public.login' });
  }
  next();
};
