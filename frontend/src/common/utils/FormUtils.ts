import type { QForm } from 'quasar';
import { notify } from 'src/common/utils/NotifyUtils';

const message = 'Existem erros no formulário, revise-o e salve novamente.';

class FormUtils {
  async validate(form: QForm | undefined): Promise<boolean> {
    if (form && typeof form === 'object') {
      try {
        const isValid = await form.validate();
        if (!isValid) {
          notify.error(message);
        }
        return isValid;
      } catch {
        notify.error(message);
        return false;
      }
    }

    notify.error(message);
    return false;
  }
}

export const formUtils = new FormUtils();
