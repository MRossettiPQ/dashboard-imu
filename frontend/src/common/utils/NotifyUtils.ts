import type { QNotifyCreateOptions } from 'quasar';
import { Notify, throttle } from 'quasar';

export const notify = {
  success: throttle((message: string, opts?: QNotifyCreateOptions) => {
    Notify.create({
      message,
      textColor: 'white',
      color: 'positive',
      icon: 'check',
      ...opts,
    });
  }, 500),
  error: throttle((message: string, opts?: QNotifyCreateOptions) => {
    Notify.create({
      message,
      textColor: 'white',
      color: 'negative',
      icon: 'priority_high',
      ...opts,
    });
  }, 500),
};
