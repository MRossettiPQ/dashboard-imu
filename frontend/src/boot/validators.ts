import { boot } from 'quasar/wrappers';
import { ValidatorsUtils } from 'src/common/utils/ValidatorUtils';

declare module 'vue' {
  interface ComponentCustomProperties {
    $rules: ValidatorsUtils;
  }
}
export default boot(({ app }) => {
  app.config.globalProperties.$rules = new ValidatorsUtils();
});
