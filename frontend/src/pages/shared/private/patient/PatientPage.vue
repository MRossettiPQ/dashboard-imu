<script setup lang="ts">
import { ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';
import { patientService } from 'src/common/services/patient/patient-service';

const pagination = ref(
  new PaginationUtils({
    service: patientService.list,
    params: {
      page: 1,
      rpp: 10,
    },
  }),
);
</script>

<template>
  <q-page class="row items-center justify-evenly">
    {{ pagination.result }}
  </q-page>
</template>

<style scoped></style>
