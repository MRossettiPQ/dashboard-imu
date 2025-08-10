import api from 'src/common/services/http-client';
import type { BasicResponse, Pagination, Patient } from 'src/common/models/models';
import { AxiosResponse } from 'src/common/models/models';
import { plainToInstance } from 'class-transformer';

export const sessionService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<BasicResponse<Patient>>> => {
    const response = await api.get<BasicResponse<Patient>>(`/api/patients/${uuid}`);
    return plainToInstance(AxiosResponse<BasicResponse<Patient>>, response);
  },
  list: async ({
    rpp,
    page,
  }: {
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<BasicResponse<Pagination<Patient>>>> => {
    const response = await api.get<BasicResponse<Pagination<Patient>>>('/api/patients', {
      params: {
        rpp,
        page,
      },
    });
    return plainToInstance(AxiosResponse<BasicResponse<Pagination<Patient>>>, response);
  },
};
