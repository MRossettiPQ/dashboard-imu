<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import type { QForm } from 'quasar';
import { ref } from 'vue';
import { formUtils } from 'src/common/utils/FormUtils';
import { useAuthStore } from 'stores/auth-store';
import { useRouter } from 'vue-router';
import { api } from 'boot/axios';
import type { RegisterDto } from 'src/common/api/generated/models';
import BackgroundNetwork from 'components/Background/BackgroundNetwork.vue';

const store = useAuthStore();
const router = useRouter();
const mainForm = ref<QForm | null>(null);
const loading = ref(false);
const form = ref<RegisterDto>({
  name: '',
  email: '',
  username: '',
  password: '',
  passwordConfirm: '',
});

async function register(): Promise<void> {
  try {
    loading.value = true;
    if (mainForm.value == null) {
      return;
    }

    await formUtils.validate(mainForm.value);

    const { data } = await api.postApiAccessRegister(form.value);
    if (data?.content?.access) {
      await store.save(data.content.access);
      await router.push({ name: 'shared.home' });
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <custom-page>
    <div class="register-wrapper">
      <!-- Neural network SVG background -->
      <background-network />

      <q-card class="register-card" flat>
        <div class="register-card__accent" />

        <div class="register-card__content">
          <div class="register-header">
            <q-icon name="person_add_alt_1" class="register-header__icon" />
            <h2 class="register-header__title">Criar conta</h2>
            <p class="register-header__subtitle">
              Preencha os dados para se registrar no Dashboard IMU
            </p>
          </div>

          <q-form ref="mainForm" class="register-form">
            <q-input
              v-model.trim="form.username"
              label="Username"
              outlined
              @keyup.enter="register"
              :rules="[$rules.notBlank]"
              class="register-input"
            >
              <template #prepend>
                <q-icon name="alternate_email" color="primary" />
              </template>
            </q-input>

            <q-input
              v-model.trim="form.name"
              label="Nome"
              outlined
              @keyup.enter="register"
              :rules="[$rules.notBlank]"
              class="register-input"
            >
              <template #prepend>
                <q-icon name="person_outline" color="primary" />
              </template>
            </q-input>

            <q-input
              v-model.trim="form.email"
              type="email"
              label="E-mail"
              outlined
              @keyup.enter="register"
              :rules="[$rules.email]"
              class="register-input"
            >
              <template #prepend>
                <q-icon name="mail_outline" color="primary" />
              </template>
            </q-input>

            <q-input
              v-model.trim="form.password"
              outlined
              type="password"
              label="Senha"
              @keyup.enter="register"
              :rules="[$rules.notBlank, $rules.minLength(8)]"
              class="register-input"
            >
              <template #prepend>
                <q-icon name="lock_outline" color="primary" />
              </template>
            </q-input>

            <q-input
              v-model.trim="form.passwordConfirm"
              outlined
              type="password"
              label="Confirme a senha"
              @keyup.enter="register"
              :rules="[
                $rules.notBlank,
                $rules.minLength(8),
                $rules.equal(form.password, 'As senhas não correspondem'),
              ]"
              class="register-input"
            >
              <template #prepend>
                <q-icon name="lock_outline" color="primary" />
              </template>
            </q-input>
          </q-form>

          <q-btn
            color="primary"
            :disable="loading"
            :loading="loading"
            label="Registrar"
            size="16px"
            unelevated
            no-caps
            class="register-btn"
            @click="register"
          />

          <p class="register-footer">Dashboard IMU &middot; Acesso seguro</p>
        </div>
      </q-card>
    </div>
  </custom-page>
</template>

<style lang="scss" scoped>
.register-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: #f1f5f9;
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
    r: 3%;
  }
  50% {
    opacity: 1;
    r: 5%;
  }
}

.register-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  margin: 24px;
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

  &__content {
    padding: 40px 36px 32px;
    display: flex;
    flex-direction: column;
    gap: 28px;
  }
}

.register-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  &__icon {
    font-size: 40px;
    color: $primary;
    background: rgba($primary, 0.08);
    border-radius: 50%;
    padding: 14px;
    margin-bottom: 4px;
  }

  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0;
    letter-spacing: -0.3px;
  }

  &__subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
    font-weight: 400;
    text-align: center;
  }
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.register-input {
  :deep(.q-field__control) {
    border-radius: 10px;
  }

  :deep(.q-field--outlined .q-field__control:hover:before) {
    border-color: $primary;
  }

  :deep(.q-field--outlined.q-field--focused .q-field__control:after) {
    border-width: 2px;
  }
}

.register-btn {
  width: 100%;
  border-radius: 10px !important;
  font-weight: 600;
  letter-spacing: 0.4px;
  padding: 10px 0 !important;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba($primary, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

.register-footer {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  margin: 0;
  letter-spacing: 0.2px;
}

@media (max-width: 480px) {
  .register-card__content {
    padding: 32px 24px 24px;
  }

  .register-header__title {
    font-size: 22px;
  }
}
</style>
