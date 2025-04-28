import type { NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw } from 'vue-router';

const publicRoutes: RouteRecordRaw[] = [
  {
    name: 'public.login',
    path: 'login',
    component: () => import('pages/public/login/LoginPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    name: 'public.register',
    path: 'register',
    component: () => import('pages/public/register/RegisterPage.vue'),
    meta: {
      private: true,
    },
  },
  {
    name: 'public.socket',
    path: 'socket',
    component: () => import('pages/public/socket/SocketPage.vue'),
    meta: {
      private: true,
    },
  },
];
const privateRoutes: RouteRecordRaw[] = [
  {
    name: 'private.account',
    path: 'account',
    component: () => import('pages/private/account/AccountPage.vue'),
  },
  {
    name: 'private.patient',
    path: 'patient',
    component: () => import('pages/private/patient/PatientPage.vue'),
  },
  {
    name: 'private.session',
    path: 'session',
    component: () => import('pages/private/session/SessionPage.vue'),
  },
];

const typeRoutes: RouteRecordRaw[] = [
  {
    name: 'shared',
    path: '/',
    component: () => import('pages/shared/home/HomePage.vue'),
  },
  {
    name: 'public',
    path: '/public',
    component: () => import('pages/public/PublicApp.vue'),
    children: publicRoutes,
    beforeEnter: async (to, from, next) => {
      console.log('Before enter - publico');
      // const token = await AuthenticationUtils.getToken(); // ou outro método de checagem
      const token = await Promise.all([]);
      const isLoggedIn = !!token;

      if (!isLoggedIn) {
        next();
      } else {
        next({ path: '/public/home' });
      }
    },
  },
  {
    name: 'private',
    path: '/private',
    component: () => import('pages/private/PrivateApp.vue'),
    children: privateRoutes,
    beforeEnter: async (to, from, next) => {
      console.log('Before enter - privados');
      // const token = await AuthenticationUtils.getToken(); // ou outro método de checagem
      const token = await Promise.all([]);
      const isLoggedIn = !!token;

      if (!isLoggedIn) {
        next({ path: '/public/login' });
      } else {
        next();
      }
    },
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
    component: () => import('pages/shared/NotFound.vue'),
  },
];

export const routeBeforeGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
): void => {
  console.log('Before enter - geral');
  // TODO Access granted without authentication
  // const accessReleased = ['access.login', 'access.register', 'access.home'];
  // TODO Hide when logged
  // const hideWhenLogged = ['access.login', 'access.register'];
  // const token = await AuthenticationUtils.getToken();
  // const isLoggedIn = !!token;
  //
  // if (to.name === from.name) {
  //   return;
  // }
  //
  // if (isLoggedIn) {
  //   if (hideWhenLogged.includes(to.name)) {
  //     next({
  //       path: '/',
  //     });
  //   } else {
  //     next();
  //   }
  // } else {
  //   if (accessReleased.includes(to.name)) {
  //     next();
  //   } else {
  //     next({
  //       path: '/login',
  //     });
  //   }
  // }
  next();
};
