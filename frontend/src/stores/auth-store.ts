import { Cookies } from 'quasar';
import { acceptHMRUpdate, defineStore } from 'pinia';
import type { AccessDto, AuthStore, UserRole } from 'src/common/models/models';
import { useRouter } from 'vue-router';
import { accessService } from 'src/common/services/access/access-service';
import { CookieType, getCookieConfiguration } from 'src/common/utils/cookieUtils';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';

let refreshTimeout: ReturnType<typeof setTimeout> | undefined;

const trimDate = (input: string | null): Dayjs | null => {
  const formated = input?.replace(/\.(\d{3})\d+/, '.$1');
  if (formated == null) return null;
  return dayjs(input?.replace(/\.(\d{3})\d+/, '.$1'));
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
    logOut(): void {
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
    },
    async checkPermission(roles: UserRole[]): Promise<void> {
      const router = useRouter();
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

      if (this.accessTokenExpiresAt && this.accessTokenExpiresAt?.isAfter(now) === true) {
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

      if (this.refreshTokenExpiresAt && this.refreshTokenExpiresAt?.isAfter(now) === true) {
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

      const router = useRouter();
      const now = dayjs();
      if (!this.accessTokenExpiresAt) {
        return;
      }

      try {
        this.loading = true;
        if (this.accessTokenExpiresAt?.isAfter(now) === true) {
          const { data } = await accessService.context();
          this.user = data.content;
          this.scheduleRefresh();
        } else {
          console.log('Session expired, redirect to login');
          this.logOut();
          await router.push({ name: 'public.login' });
        }
      } catch (e) {
        console.error('Error on loadContext', e);
        this.logOut();
        await router.push({ name: 'public.login' });
      } finally {
        this.loading = false;
      }
    },
    async save(data: AccessDto, rememberMe: boolean = false) {
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.accessTokenExpiresAt = trimDate(String(data.accessTokenExpiresAt));
      this.refreshTokenExpiresAt = trimDate(String(data.refreshTokenExpiresAt));

      if (this.accessTokenExpiresAt == null || this.refreshTokenExpiresAt == null) return;

      const accessTokenExpiry = this.accessTokenExpiresAt.toDate();
      const confAccess = getCookieConfiguration(accessTokenExpiry);
      Cookies.set(CookieType.ACCESS_TOKEN, this.accessToken, confAccess);
      Cookies.set(CookieType.ACCESS_TOKEN_DATE, accessTokenExpiry.toISOString(), confAccess);

      if (rememberMe) {
        const refreshTokenExpiry = this.refreshTokenExpiresAt.toDate();
        const confRefresh = getCookieConfiguration(refreshTokenExpiry);
        Cookies.set(CookieType.REFRESH_TOKEN, this.accessToken, confRefresh);
        Cookies.set(CookieType.REFRESH_TOKEN_DATE, refreshTokenExpiry.toISOString(), confRefresh);
      }

      this.scheduleRefresh();
      await this.loadContext();
    },
    scheduleRefresh() {
      if (refreshTimeout) clearTimeout(refreshTimeout);

      if (this.accessTokenExpiresAt != null) {
        const now = dayjs();
        const refreshTime = this.accessTokenExpiresAt;

        if (refreshTime.isBefore(now)) {
          const delay = refreshTime.toDate().getTime() - now.toDate().getTime();
          refreshTimeout = setTimeout(() => {
            this.refreshTokens().catch((e) => console.error(e));
          }, delay);
        }
      }
    },
    async refreshTokens(): Promise<void> {
      console.log('Checking refresh token');
      const router = useRouter();
      if (!this.refreshToken) throw new Error('No refresh token available');

      const { data } = await accessService.refreshToken(this.refreshToken);
      if (!data?.content?.access) throw new Error('No content in response');
      try {
        await this.save(data.content.access);
        this.scheduleRefresh();
      } catch (e) {
        console.error('Error refreshing token', e);
        this.logOut();
        await router.push({ name: 'public.login' });
      }
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot));
}
