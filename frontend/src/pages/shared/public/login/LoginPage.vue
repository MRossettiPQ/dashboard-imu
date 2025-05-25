<script setup lang="ts">
import { ref } from 'vue';
import { accessService } from 'src/common/services/access/access-service';
import type { LoginRequestDto } from 'src/common/models/models';
import { formUtils } from 'src/common/utils/FormUtils';
import { QForm } from 'quasar';

// 👇 Captura o ref do form
const mainForm = ref<QForm | null>(null);
const loading = ref(false);
const form = ref<LoginRequestDto>({
  username: '',
  password: '',
});

async function login(): Promise<void> {
  try {
    loading.value = true;
    if (mainForm.value == null || (form.value.username === '' && form.value.password === '')) {
      return;
    }

    await formUtils.validate(mainForm.value);

    await accessService.login({ form: form.value });
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <q-card class="access-card self-center">
    <q-card-section horizontal class="column gap-15 justify-center">
      <div class="column">
        <div class="row items-center">
          <h5 class="title col">Login</h5>
        </div>
      </div>
      <q-form ref="mainForm" class="h-100 w-100">
        <q-card-section class="col self-center column gap-20">
          <q-input
            v-model.trim="form.username"
            label="Username"
            dense
            outlined
            @keyup.enter="login"
            :rules="[$rules.notBlank, $rules.email]"
          />

          <div class="col column gap-5">
            <q-input
              v-model.trim="form.password"
              dense
              outlined
              type="password"
              label="Senha"
              @keyup.enter="login"
              :rules="[$rules.notBlank, $rules.minLength(8)]"
            />
            <router-link class="p-0 m-0 router-link" to="recovery-password">
              Esqueci minha senha
            </router-link>
          </div>

          <q-btn
            color="primary"
            :disable="loading"
            :loading="loading"
            label="Entrar"
            size="16px"
            @click="login"
          />
        </q-card-section>
      </q-form>
    </q-card-section>
  </q-card>
</template>

<style scoped></style>
