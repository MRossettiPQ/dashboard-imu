import api from 'src/common/services/http-client';
import { PatientPaginationResponse } from 'src/common/models/models';
import { PatientResponse } from 'src/common/models/models';
import type { Patient, AxiosResponse, BasicResponse, Pagination } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';

export const patientService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<BasicResponse<Patient>>> => {
    const response = await api.get<BasicResponse<Patient>>(`/api/patients/${uuid}`);
    return convertResponse(response, PatientResponse);
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
    return convertResponse(response, PatientPaginationResponse);
  },
};
