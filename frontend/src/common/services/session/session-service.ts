import api from 'src/common/services/http-client';
import { SessionResponse } from 'src/common/models/models';
import { SessionPaginationResponse } from 'src/common/models/models';
import type { Pagination, AxiosResponse, BasicResponse, Patient } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';

export const sessionService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<BasicResponse<Patient>>> => {
    const response = await api.get<BasicResponse<Patient>>(`/api/sessions/${uuid}`);
    return convertResponse(response, SessionResponse);
  },
  list: async ({
    rpp,
    page,
  }: {
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<BasicResponse<Pagination<Patient>>>> => {
    const response = await api.get<BasicResponse<Pagination<Patient>>>('/api/sessions', {
      params: {
        rpp,
        page,
      },
    });
    return convertResponse(response, SessionPaginationResponse);
  },
};
