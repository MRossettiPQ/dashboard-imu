<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { ref } from 'vue';
import type { QForm } from 'quasar';
import type { LoginRequestDto } from 'src/common/models/models';
import { accessService } from 'src/common/services/access/access-service';
import { formUtils } from 'src/common/utils/FormUtils';
import { useAuthStore } from 'stores/auth-store';
import { useRouter } from 'vue-router';

const store = useAuthStore();
const router = useRouter();
const mainForm = ref<QForm | null>(null);
const loading = ref(false);
const form = ref<LoginRequestDto>({
  username: '',
  password: '',
});

async function login(): Promise<void> {
  try {
    loading.value = true;
    if (mainForm.value == null) {
      return;
    }
    if (form.value.username.trim() === '' && form.value.password.trim() === '') {
      return;
    }

    await formUtils.validate(mainForm.value);

    const { data, status } = await accessService.login({ form: form.value });
    console.log(data, status);
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
    <div class="flex u-h-100 u-w-100 justify-center items-center content-center">
      <q-card
        bordered
        class="self-center column u-w-100 flex column justify-between u-p-24 no-wrap u-gap-30"
        style="max-width: 530px"
      >
        <div class="column">
          <div class="row items-center">
            <h2 class="col text-center u-p-12 u-m-0" style="font-size: 52px">Login</h2>
          </div>
        </div>

        <q-form ref="mainForm" class="flex u-w-100 column u-gap-30">
          <q-input
            v-model.trim="form.username"
            label="Username"
            outlined
            @keyup.enter="login"
            :rules="[$rules.notBlank]"
          />

          <div class="column gap-5">
            <q-input
              v-model.trim="form.password"
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
        </q-form>

        <q-btn
          color="primary"
          :disable="loading"
          :loading="loading"
          label="Entrar"
          size="16px"
          @click="login"
        />
      </q-card>
    </div>
  </custom-page>
</template>
