import api from 'src/common/services/http-client';
import type { AxiosResponse } from 'src/common/models/models';
import { jsonConverter } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';
import type { Patient } from 'src/common/models/patient/Patient';
import { PatientBasicResponse, PatientPaginationResponse } from 'src/common/models/patient/Patient';
import { SessionPaginationResponse } from 'src/common/models/session/Session';

export const patientService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<PatientBasicResponse>> => {
    const response = await api.get(`/api/patients/${uuid}`);
    return convertResponse(response, PatientBasicResponse);
  },
  save: async ({ form }: { form: Patient }): Promise<AxiosResponse<PatientBasicResponse>> => {
    const response = await api.post(`/api/patients/`, jsonConverter(form));
    return convertResponse(response, PatientBasicResponse);
  },
  list: async ({
    rpp,
    page,
  }: {
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<PatientPaginationResponse>> => {
    const response = await api.get('/api/patients', {
      params: {
        rpp,
        page,
      },
    });
    return convertResponse(response, PatientPaginationResponse);
  },
  listSession: async ({
    patientId,
    rpp,
    page,
  }: {
    patientId: string;
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<SessionPaginationResponse>> => {
    const response = await api.get(`/api/patients/${patientId}/sessions`, {
      params: {
        rpp,
        page,
      },
    });
    return convertResponse(response, SessionPaginationResponse);
  },
};
