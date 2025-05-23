import { defineStore, acceptHMRUpdate } from 'pinia';
import type { AccessDto, AuthStore, UserRole } from 'src/common/models/models';
import { useRouter } from 'vue-router';
import { accessService } from 'src/common/services/access/access-service';
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
      this.accessToken = getAuthToken();
      this.refreshToken = getRefreshToken();
      this.accessTokenExpiresAt = getAccessTokenExpiresAt();
      this.refreshTokenExpiresAt = getRefreshTokenExpiresAt();
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
      const router = useRouter();

      this.accessToken = getAuthToken();
      this.refreshToken = getRefreshToken();
      this.accessTokenExpiresAt = getAccessTokenExpiresAt();
      this.refreshTokenExpiresAt = getRefreshTokenExpiresAt();
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
          await this.logOut();
          await router.push({ name: 'login' });
        }
      } catch (e) {
        console.error('Error on loadContext', e);
        await this.logOut();
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
      saveAuthData(data, rememberMe);

      this.scheduleRefresh();
      await this.loadContext();
    },
    async logOut(): Promise<void> {
      this.accessToken = null;
      this.refreshToken = null;
      this.accessTokenExpiresAt = null;
      this.refreshTokenExpiresAt = null;
      this.user = null;

      await clearAuthData();
      if (refreshTimeout) clearTimeout(refreshTimeout);
    },
    scheduleRefresh() {
      if (refreshTimeout) clearTimeout(refreshTimeout);

      if (this.accessTokenExpiresAt != null && this.refreshToken != null) {
        const now = new Date();
        const refreshTime = this.accessTokenExpiresAt;

        if (refreshTime.isBefore(now)) {
          refreshTimeout = setTimeout(async () => {
            console.log('Checking refresh token');
            await this.refreshTokens();
          }, refreshTime.toDate().getTime() - now.getTime());
        }
      }
    },
    async refreshTokens() {
      const router = useRouter();
      if (!this.refreshToken) throw new Error('No refresh token available');

      try {
        const { data } = await accessService.refreshToken(this.refreshToken);
        if (!data.content) new Error('No content in response');

        await this.save(data.content);

        this.scheduleRefresh();
      } catch (e) {
        console.error('Error refreshing token', e);
        await this.logOut();
        await router.push({ name: 'login' });
      }
    },
  },
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot));
}
