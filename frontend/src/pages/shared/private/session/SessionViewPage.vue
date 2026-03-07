<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { notify } from 'src/common/utils/NotifyUtils';
import { api } from 'boot/axios';
import type { SessionDto } from 'src/common/api/generated/models';
import { SessionType } from 'src/common/api/generated/models';

const route = useRoute();
const router = useRouter();
const uuid = computed(() => route.params?.['uuid']?.toString());
const form = ref<SessionDto>({
  id: null,
  sessionDate: null,
  type: SessionType.REAL,
  observation: null,
  patient: null,
  physiotherapist: null,
  articulations: [],
});

onMounted(async () => {
  if (!uuid.value) {
    notify.error('Obrigatório o identificador da sessão');
    return await router.push({ name: 'private.session' });
  }
  const { data } = await api.getApiSessionsUuid(uuid.value);
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
