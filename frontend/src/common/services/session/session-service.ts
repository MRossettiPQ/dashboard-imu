import api from 'src/common/services/http-client';
import type { BasicResponse, Pagination, Patient } from 'src/common/models/models';

export const sessionService = {
  get: async ({ uuid }: { uuid: string }) => {
    return await api.get<BasicResponse<Patient>>(`/api/patients/${uuid}`);
  },
  list: async ({ rpp, page }: { rpp: number; page: number }) => {
    return await api.get<BasicResponse<Pagination<Patient>>>('/api/patients', {
      params: {
        rpp,
        page,
      },
    });
  },
};
