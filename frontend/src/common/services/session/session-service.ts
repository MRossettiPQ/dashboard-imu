import api from 'src/common/services/http-client';
import type { AxiosResponse } from 'src/common/models/models';
import { convertResponse } from 'src/common/models/models';
import { SessionBasicResponse, SessionPaginationResponse } from 'src/common/models/session/Session';

export const sessionService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<SessionBasicResponse>> => {
    const response = await api.get(`/api/sessions/${uuid}`);
    return convertResponse(response, SessionBasicResponse);
  },
  list: async ({
    rpp,
    page,
  }: {
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<SessionPaginationResponse>> => {
    const response = await api.get('/api/sessions', {
      params: {
        rpp,
        page,
      },
    });
    return convertResponse(response, SessionPaginationResponse);
  },
};
