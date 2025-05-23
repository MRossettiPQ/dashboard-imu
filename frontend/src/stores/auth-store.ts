import { Cookies } from 'quasar';
import { acceptHMRUpdate, defineStore } from 'pinia';
import type { AccessDto, AuthStore, UserRole } from 'src/common/models/models';
import { useRouter } from 'vue-router';
import { accessService } from 'src/common/services/access/access-service';
import { CookieType, getCookieConfiguration } from 'src/common/utils/cookieUtils';

let refreshTimeout: ReturnType<typeof setTimeout> | undefined;

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
      this.accessToken = Cookies.get('accessToken');
      this.refreshToken = Cookies.get('refreshToken');
      this.accessTokenExpiresAt = Cookies.get('accessTokenExpiresAt');
      this.refreshTokenExpiresAt = Cookies.get('refreshTokenExpiresAt');
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
        await router.push({ name: 'login' });
        return;
      }

      if (roles.length && !roles.includes(this.user.role)) {
        await router.push({ name: 'forbidden' });
      }
    },
    async isAuthenticatedOrLoadContext(): Promise<boolean> {
      this.loadCookies();
      const now = new Date();

      if (this.accessTokenExpiresAt && this.accessTokenExpiresAt.isAfter(now)) {
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

      if (this.refreshTokenExpiresAt && this.refreshTokenExpiresAt.isAfter(now)) {
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
      const now = new Date();
      if (!this.accessTokenExpiresAt || !this.refreshTokenExpiresAt) {
        return;
      }

      try {
        this.loading = true;
        if (this.accessTokenExpiresAt?.isAfter(now) || this.refreshTokenExpiresAt?.isAfter(now)) {
          const { data } = await accessService.context();
          this.user = data.content;
          this.scheduleRefresh();
        } else {
          console.log('Session expired, redirect to login');
          this.logOut();
          await router.push({ name: 'login' });
        }
      } catch (e) {
        console.error('Error on loadContext', e);
        this.logOut();
        await router.push({ name: 'login' });
      } finally {
        this.loading = false;
      }
    },
    async save(data: AccessDto, rememberMe: boolean = false) {
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.accessTokenExpiresAt = data.accessTokenExpiresAt;
      this.refreshTokenExpiresAt = data.refreshTokenExpiresAt;

      const accessTokenExpiry = data.accessTokenExpiresAt;
      const confAccess = getCookieConfiguration(accessTokenExpiry.toDate());
      Cookies.set(CookieType.ACCESS_TOKEN, data.accessToken, confAccess);
      Cookies.set(CookieType.ACCESS_TOKEN_DATE, accessTokenExpiry.toISOString(), confAccess);

      if (rememberMe) {
        const refreshTokenExpiry = data.refreshTokenExpiresAt;
        const confRefresh = getCookieConfiguration(refreshTokenExpiry.toDate());
        Cookies.set(CookieType.REFRESH_TOKEN, data.accessToken, confRefresh);
        Cookies.set(CookieType.ACCESS_TOKEN_DATE, refreshTokenExpiry.toISOString(), confRefresh);
      }

      this.scheduleRefresh();
      await this.loadContext();
    },
    scheduleRefresh() {
      if (refreshTimeout) clearTimeout(refreshTimeout);

      if (this.accessTokenExpiresAt != null && this.refreshToken != null) {
        const now = new Date();
        const refreshTime = this.accessTokenExpiresAt;

        if (refreshTime.isBefore(now)) {
          const delay = refreshTime.toDate().getTime() - now.getTime();
          refreshTimeout = setTimeout(this.refreshTokens, delay);
        }
      }
    },
    async refreshTokens(): Promise<void> {
      console.log('Checking refresh token');
      const router = useRouter();
      if (!this.refreshToken) throw new Error('No refresh token available');

      try {
        const { data } = await accessService.refreshToken(this.refreshToken);
        if (!data.content) new Error('No content in response');

        await this.save(data.content!);
        this.scheduleRefresh();
      } catch (e) {
        console.error('Error refreshing token', e);
        this.logOut();
        await router.push({ name: 'login' });
      }
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot));
}
