import { boot } from 'quasar/wrappers';
import CustomPage from 'components/CustomPage/CustomPage.vue';
// import Vue3Lottie from 'vue3-lottie';

// interface Component {
//   default: never;
// }

export default boot(async ({ app }): Promise<void> => {
  console.log('[INIT] - Loading components');
  // src/components/*.vue
  // const modules = import.meta.glob('../components/**/*.vue');
  //
  // for (const path in modules) {
  //   const fileName = path.substring(path.lastIndexOf('/') + 1);
  //   const name = fileName.substring(0, fileName.indexOf('.'));
  //   const component = (await modules[path]()) as Component;
  //   app.component(name, component.default);
  // }

  // app.use(Vue3Lottie);
  app.component('CustomPage', CustomPage);
});
