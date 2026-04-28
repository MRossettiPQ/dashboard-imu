import { Cookies } from 'quasar';
import { acceptHMRUpdate, defineStore } from 'pinia';
import type { AuthStore } from 'src/common/api/manual/AuthStore';
import { CookieType, getCookieConfiguration } from 'src/common/utils/cookieUtils';
import dayjs from 'dayjs';
import { api } from 'boot/axios';
import type { AccessDto, UserRole } from 'src/common/api/generated/models';
import { getRouterInstance } from 'src/router';

let refreshTimeout: ReturnType<typeof setTimeout> | undefined;

const trimDate = (input: string | null): Date | null => {
  const formated = input?.replace(/\.(\d{3})\d+/, '.$1');
  if (formated == null) return null;
  return dayjs(input?.replace(/\.(\d{3})\d+/, '.$1')).toDate();
};

const ensureDate = (value: string | Date | null | undefined): Date | null => {
  if (!value) return null;
  return value instanceof Date ? value : new Date(value);
};

export const useAuthStore = defineStore('auth', {
  state: (): AuthStore => ({
    loading: false,
    user: null,
    accessToken: null,
    refreshToken: null,
    accessTokenExpiresAt: null,
    refreshTokenExpiresAt: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.user,
    isLoading: (state) => state.loading,
  },

  actions: {
    loadCookies(): void {
      this.accessToken = Cookies.get(CookieType.ACCESS_TOKEN);
      this.refreshToken = Cookies.get(CookieType.REFRESH_TOKEN);
      this.accessTokenExpiresAt = trimDate(Cookies.get(CookieType.ACCESS_TOKEN_DATE));
      this.refreshTokenExpiresAt = trimDate(Cookies.get(CookieType.REFRESH_TOKEN_DATE));
    },
    async logOut(): Promise<void> {
      const router = getRouterInstance();
      this.user = null;
      this.accessToken = null;
      this.refreshToken = null;
      this.accessTokenExpiresAt = null;
      this.refreshTokenExpiresAt = null;

      Cookies.remove(CookieType.ACCESS_TOKEN);
      Cookies.remove(CookieType.ACCESS_TOKEN_DATE);
      Cookies.remove(CookieType.REFRESH_TOKEN);
      Cookies.remove(CookieType.REFRESH_TOKEN_DATE);

      if (refreshTimeout) clearTimeout(refreshTimeout);
      await router.push({ name: 'public.login' });
    },
    async checkPermission(roles: UserRole[]): Promise<void> {
      const router = getRouterInstance();
      const authenticated = await this.isAuthenticatedOrLoadContext();
      if (!authenticated || this.user?.role == undefined) {
        await router.push({ name: 'public.login' });
        return;
      }

      if (roles.length && !roles.includes(this.user.role)) {
        await router.push({ name: 'public.forbidden' });
      }
    },
    async isAuthenticatedOrLoadContext(): Promise<boolean> {
      this.loadCookies();
      const now = new Date();

      if (this.accessTokenExpiresAt && this.accessTokenExpiresAt > now) {
        if (!this.user) {
          try {
            await this.loadContext();
          } catch (e) {
            console.error('Failed to load context', e);
            return false;
          }
        }
        return true;
      }

      if (this.refreshTokenExpiresAt && this.refreshTokenExpiresAt > now) {
        try {
          await this.refreshTokens();
          await this.loadContext();
          return true;
        } catch (e) {
          console.error('Failed to refresh token', e);
          return false;
        }
      }

      return false;
    },
    async loadContext() {
      this.loadCookies();

      const router = getRouterInstance();
      const now = new Date();

      if (!this.accessTokenExpiresAt) {
        return;
      }

      try {
        this.loading = true;

        if (this.accessTokenExpiresAt > now) {
          const { data } = await api.getApiAccessContext();
          this.user = data.content;
          this.scheduleRefresh();
        } else {
          console.log('Session expired, redirect to login');
          await this.logOut();
          await router.push({ name: 'public.login' });
        }
      } catch (e) {
        console.error('Error on loadContext', e);
        await this.logOut();
        await router.push({ name: 'public.login' });
      } finally {
        this.loading = false;
      }
    },
    async save(data: AccessDto, rememberMe: boolean = false) {
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.accessTokenExpiresAt = ensureDate(data.accessTokenExpiresAt);
      this.refreshTokenExpiresAt = ensureDate(data.refreshTokenExpiresAt);

      if (this.accessTokenExpiresAt == null || this.refreshTokenExpiresAt == null) return;

      const accessTokenExpiry = this.accessTokenExpiresAt;
      const confAccess = getCookieConfiguration(accessTokenExpiry);
      Cookies.set(CookieType.ACCESS_TOKEN, this.accessToken!, confAccess);
      Cookies.set(CookieType.ACCESS_TOKEN_DATE, accessTokenExpiry.toISOString(), confAccess);

      if (rememberMe) {
        const refreshTokenExpiry = this.refreshTokenExpiresAt;
        const confRefresh = getCookieConfiguration(refreshTokenExpiry);
        Cookies.set(CookieType.REFRESH_TOKEN, this.accessToken!, confRefresh);
        Cookies.set(CookieType.REFRESH_TOKEN_DATE, refreshTokenExpiry.toISOString(), confRefresh);
      }

      this.scheduleRefresh();
      await this.loadContext();
    },
    scheduleRefresh() {
      if (refreshTimeout) clearTimeout(refreshTimeout);

      if (this.accessTokenExpiresAt != null) {
        const now = new Date();
        const refreshTime = this.accessTokenExpiresAt;

        if (refreshTime > now) {
          const delay = refreshTime.getTime() - now.getTime();

          refreshTimeout = setTimeout(() => {
            this.refreshTokens().catch((e) => console.error(e));
          }, delay);
        }
      }
    },
    async refreshTokens(): Promise<void> {
      console.log('Checking refresh token');
      const router = getRouterInstance();
      if (!this.refreshToken) throw new Error('No refresh token available');

      const { data } = await api.postApiAccessRefresh({ refreshToken: this.refreshToken });
      if (!data?.content?.access) throw new Error('No content in response');
      try {
        await this.save(data.content.access);
        this.scheduleRefresh();
      } catch (e) {
        console.error('Error refreshing token', e);
        await this.logOut();
        await router.push({ name: 'public.login' });
      }
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot));
}
