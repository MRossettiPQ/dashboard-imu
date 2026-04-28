import { defineRouter } from '#q-app/wrappers';
import type { Router } from 'vue-router';
import {
  createMemoryHistory,
  createRouter,
  createWebHashHistory,
  createWebHistory,
} from 'vue-router';
import 'reflect-metadata'; // <== IMPORTANTE para class-transformer funcionar
import { routeBeforeGuard, routes } from './routes';

/*
 * If not building with SSR mode, you can
 * directly export the Router instantiation;
 *
 * The function below can be async too; either use
 * async/await or return a Promise which resolves
 * with the Router instance.
 */

let routerInstance: Router | null = null;

export function setRouterInstance(router: Router): void {
  routerInstance = router;
}

export function getRouterInstance(): Router {
  if (!routerInstance) {
    throw new Error('Router instance not set. Call setRouterInstance() in router/index.ts');
  }
  return routerInstance;
}

export default defineRouter(function (/* { store, ssrContext } */) {
  const createHistory = process.env.SERVER
    ? createMemoryHistory
    : process.env.VUE_ROUTER_MODE === 'history'
      ? createWebHistory
      : createWebHashHistory;

  const Router = createRouter({
    scrollBehavior: () => ({ left: 0, top: 0 }),
    routes,

    // Leave this as is and make changes in quasar.conf.js instead!
    // quasar.conf.js -> build -> vueRouterMode
    // quasar.conf.js -> build -> publicPath
    history: createHistory(process.env.VUE_ROUTER_BASE),
  });

  // Set instance BEFORE registering guards,
  // so the store can access the router inside beforeEach
  setRouterInstance(Router);

  Router.beforeEach(routeBeforeGuard);
  return Router;
});
