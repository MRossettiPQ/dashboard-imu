<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import type { QForm } from 'quasar';
import { ref } from 'vue';
import type { RegisterDto } from 'src/common/models/models';
import { accessService } from 'src/common/services/access/access-service';
import { formUtils } from 'src/common/utils/FormUtils';
import { useAuthStore } from 'stores/auth-store';
import { useRouter } from 'vue-router';

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

    const { data } = await accessService.register({ form: form.value });
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
        class="self-center column u-w-100 flex justify-between u-p-24 no-wrap u-gap-30"
        style="max-width: 530px"
      >
        <div class="column">
          <div class="row items-center">
            <h2 class="col text-center u-p-12 u-m-0" style="font-size: 52px">Registrar</h2>
          </div>
        </div>

        <q-form ref="mainForm" class="flex u-w-100 column u-gap-30">
          <q-input
            v-model.trim="form.username"
            label="Username"
            outlined
            @keyup.enter="register"
            :rules="[$rules.notBlank]"
          />

          <q-input
            v-model.trim="form.name"
            label="Nome"
            outlined
            @keyup.enter="register"
            :rules="[$rules.notBlank]"
          />

          <q-input
            v-model.trim="form.email"
            type="email"
            label="E-mail"
            outlined
            @keyup.enter="register"
            :rules="[$rules.email]"
          />

          <q-input
            v-model.trim="form.password"
            outlined
            type="password"
            label="Senha"
            @keyup.enter="register"
            :rules="[$rules.notBlank, $rules.minLength(8)]"
          />

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
          />
        </q-form>

        <q-btn
          color="primary"
          :disable="loading"
          :loading="loading"
          label="Entrar"
          size="16px"
          @click="register"
        />
      </q-card>
    </div>
  </custom-page>
</template>
