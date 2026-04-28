import { boot } from 'quasar/wrappers';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import LoadDiv from 'components/LoadDiv/LoadDiv.vue';
import ErrorDiv from 'components/ErrorDiv/ErrorDiv.vue';

export default boot(({ app }): void => {
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
  app.component('LoadDiv', LoadDiv);
  app.component('ErrorDiv', ErrorDiv);
});
