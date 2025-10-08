<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Session } from 'src/common/models/session/Session';
import { sessionService } from 'src/common/services/session/session-service';
import { notify } from 'src/common/utils/NotifyUtils';

const route = useRoute();
const router = useRouter();
const uuid = computed(() => route.params?.['uuid']?.toString());
const form = ref<Session>(new Session());

onMounted(async () => {
  if (!uuid.value) {
    notify.error('Obrigatório o identificador da sessão');
    return await router.push({ name: 'private.session' });
  }

  const { data } = await sessionService.get({ uuid: uuid.value });
  if (!data.content) {
    notify.error('Sessão não encontrada');
    return await router.push({ name: 'private.session' });
  }
  form.value = data.content!;
});
</script>

<template>
  <custom-page>
    <div>{{ uuid }}</div>
    <div>{{ form }}</div>
  </custom-page>
</template>
