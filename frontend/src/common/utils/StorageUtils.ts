import dayjs from 'dayjs';

class StorageUtils {
  store(key: string, value: unknown, timeout = null): void {
    if (key && value) {
      value = JSON.stringify(value);
      window.localStorage[key] = value;
      if (timeout) {
        window.localStorage[key + '-timeout'] = JSON.stringify({
          timeout: timeout,
          date: dayjs().toISOString(),
        });
      }
    }
  }

  get(key: string): string | null {
    let timeout = window.localStorage[key + '-timeout'];
    if (timeout && timeout !== 'undefined') {
      timeout = JSON.parse(timeout);
      if (dayjs().diff(timeout.date, 'minutes') > timeout.timeout) {
        return null;
      }
    }

    const result = window.localStorage[key];
    if (result && result !== 'undefined') {
      return JSON.parse(result);
    }

    return null;
  }

  remove(key: string): void {
    window.localStorage[key] = undefined;
    window.localStorage[key + '-timeout'] = undefined;
    window.localStorage.removeItem(key);
    window.localStorage.removeItem(key + '-timeout');
  }

  eraseLocalStorage(): void {
    window.localStorage.clear();
  }

  getToken(): string | null {
    return this.get('token');
  }

  saveToken(token: string): void {
    this.store('token', token);
  }

  removeToken(): void {
    this.remove('token');
  }
}
const Storage = new StorageUtils();
export { StorageUtils, Storage as default };
