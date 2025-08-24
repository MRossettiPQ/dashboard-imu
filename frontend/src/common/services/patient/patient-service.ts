import api from 'src/common/services/http-client';
import type { Pagination, AxiosResponse } from 'src/common/models/models';
import { BasicResponse } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';
import type { Patient } from 'src/common/models/patient/Patient';

export const patientService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<BasicResponse<Patient>>> => {
    const response = await api.get<BasicResponse<Patient>>(`/api/patients/${uuid}`);
    return convertResponse(response, BasicResponse<Patient>);
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
    return convertResponse(response, BasicResponse<Pagination<Patient>>);
  },
};
