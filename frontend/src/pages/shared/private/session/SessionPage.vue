<script setup lang="ts">
import CustomPage from 'components/CustomPage/CustomPage.vue';
import CustomPagination from 'components/CustomPagination/CustomPagination.vue';
import type { QTableColumn } from 'quasar';
import { patientService } from 'src/common/services/patient/patient-service';
import { ref } from 'vue';
import PaginationUtils from 'src/common/utils/PaginationUtils';

const pagination = ref(
  new PaginationUtils({
    service: patientService.list,
    params: { page: 1, rpp: 10 },
  }),
);

const columns: QTableColumn[] = [];
</script>

<template>
  <custom-page>
    <custom-pagination :service="pagination" :columns="columns" />
  </custom-page>
</template>

<style scoped></style>
