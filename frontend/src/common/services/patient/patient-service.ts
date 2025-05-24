import api from 'src/common/services/http-client';
import type { BasicResponse, Pagination, Patient } from 'src/common/models/models';

export const patientService = {
  get: async ({ uuid }: { uuid: string }) => {
    return await api.get<BasicResponse<Patient>>(`users/${uuid}`);
  },
  list: async ({ rpp, page }: { rpp: number; page: number }) => {
    return await api.get<Pagination<Patient>>('users', {
      params: {
        rpp,
        page,
      },
    });
  },
};
