<script setup lang="ts">
import { useAuthStore } from 'stores/auth-store';
import CustomPage from 'components/CustomPage/CustomPage.vue';
import BackgroundNetwork from 'components/Background/BackgroundNetwork.vue';

const store = useAuthStore();
</script>

<template>
  <custom-page>
    <div class="profile-wrapper">
      <!-- Neural network SVG background -->
      <background-network />

      <div class="profile-container">
        <q-card class="profile-card" flat>
          <div class="profile-card__accent" />

          <div class="profile-card__header">
            <q-avatar size="88px" class="profile-avatar">
              <span class="profile-avatar__initials">
                {{ store.user?.name?.charAt(0)?.toUpperCase() ?? 'U' }}
              </span>
            </q-avatar>

            <h2 class="profile-card__name">{{ store.user?.name ?? '—' }}</h2>
            <p class="profile-card__username">@{{ store.user?.username ?? '—' }}</p>
          </div>
        </q-card>

        <q-card class="profile-card" flat>
          <div class="profile-card__accent" />

          <div class="profile-card__body">
            <h3 class="profile-section-title">
              <q-icon name="info" class="profile-section-title__icon" />
              Informações da conta
            </h3>

            <div class="profile-info-grid">
              <div class="profile-info-item">
                <div class="profile-info-item__icon-wrap">
                  <q-icon name="alternate_email" color="primary" size="20px" />
                </div>
                <div class="profile-info-item__content">
                  <span class="profile-info-item__label">Username</span>
                  <span class="profile-info-item__value">{{ store.user?.username ?? '—' }}</span>
                </div>
              </div>

              <div class="profile-info-item">
                <div class="profile-info-item__icon-wrap">
                  <q-icon name="person_outline" color="primary" size="20px" />
                </div>
                <div class="profile-info-item__content">
                  <span class="profile-info-item__label">Nome</span>
                  <span class="profile-info-item__value">{{ store.user?.name ?? '—' }}</span>
                </div>
              </div>

              <div class="profile-info-item">
                <div class="profile-info-item__icon-wrap">
                  <q-icon name="mail_outline" color="primary" size="20px" />
                </div>
                <div class="profile-info-item__content">
                  <span class="profile-info-item__label">E-mail</span>
                  <span class="profile-info-item__value">{{ store.user?.email ?? '—' }}</span>
                </div>
              </div>

              <div class="profile-info-item">
                <div class="profile-info-item__icon-wrap">
                  <q-icon name="shield" color="primary" size="20px" />
                </div>
                <div class="profile-info-item__content">
                  <span class="profile-info-item__label">Perfil de acesso</span>
                  <span class="profile-info-item__value">{{ store.user?.role ?? '—' }}</span>
                </div>
              </div>

              <div class="profile-info-item">
                <div class="profile-info-item__icon-wrap">
                  <q-icon name="verified_user" color="primary" size="20px" />
                </div>
                <div class="profile-info-item__content">
                  <span class="profile-info-item__label">Status</span>
                  <span class="profile-info-item__value">
                    <q-badge
                      :color="store.user?.active ? 'positive' : 'negative'"
                      :label="store.user?.active ? 'Ativo' : 'Inativo'"
                    />
                  </span>
                </div>
              </div>
            </div>
          </div>
        </q-card>
      </div>
    </div>
  </custom-page>
</template>

<style lang="scss" scoped>
.profile-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  width: 100%;
  min-height: 0;
  overflow: hidden;
  background: #f1f5f9;
  padding: 10px 4px;
}

.neural-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  color: $primary;
  opacity: 0.18;
}

.edge {
  stroke-dasharray: 6 4;
  animation: dash 30s linear infinite;

  &--1 {
    animation-duration: 28s;
    opacity: 0.6;
  }
  &--2 {
    animation-duration: 34s;
    opacity: 0.45;
    animation-direction: reverse;
  }
  &--3 {
    animation-duration: 40s;
    opacity: 0.55;
  }
}

@keyframes dash {
  to {
    stroke-dashoffset: -200;
  }
}

.node {
  opacity: 0.7;

  &--1 {
    animation: pulse 4s ease-in-out infinite;
  }
  &--2 {
    animation: pulse 5s ease-in-out 1s infinite;
  }
  &--3 {
    animation: pulse 6s ease-in-out 2s infinite;
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.4;
    r: 3;
  }
  50% {
    opacity: 1;
    r: 5;
  }
}

.profile-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 520px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border-radius: 16px !important;
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.04),
    0 10px 24px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(0, 0, 0, 0.03);
  background: #ffffff;
  overflow: hidden;

  &__accent {
    height: 5px;
    background: linear-gradient(90deg, $primary, lighten($primary, 18%));
  }

  &__header {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 36px 36px 32px;
    gap: 10px;
  }

  &__name {
    font-size: 24px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    letter-spacing: -0.3px;
  }

  &__username {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
    font-weight: 400;
  }

  &__badge {
    margin-top: 4px;
    font-size: 12px;
    padding: 4px 14px;
    border-radius: 20px;
  }

  &__body {
    padding: 32px 36px;
    display: flex;
    flex-direction: column;
    gap: 24px;
  }
}

.profile-avatar {
  background: linear-gradient(135deg, $primary, lighten($primary, 14%));
  box-shadow: 0 6px 20px rgba($primary, 0.3);
  margin-bottom: 4px;

  &__initials {
    font-size: 36px;
    font-weight: 700;
    color: #ffffff;
    line-height: 1;
    user-select: none;
  }
}

.profile-section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;

  &__icon {
    font-size: 20px;
    color: $primary;
  }
}

.profile-info-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  transition: background 0.2s ease;

  &:hover {
    background: rgba($primary, 0.04);
  }

  &__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: rgba($primary, 0.08);
    flex-shrink: 0;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__label {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.4px;
  }

  &__value {
    font-size: 15px;
    color: #1a1a2e;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 480px) {
  .profile-wrapper {
    padding: 24px 12px;
  }

  .profile-card__header {
    padding: 28px 24px 24px;
  }

  .profile-card__body {
    padding: 24px 20px;
  }

  .profile-card__name {
    font-size: 20px;
  }
}
</style>
