import api from 'src/common/services/http-client';
import type { Pagination, AxiosResponse } from 'src/common/models/models';
import { BasicResponse, convertResponse } from 'src/common/models/models';
import type { Session } from 'src/common/models/session/Session';

export const sessionService = {
  get: async ({ uuid }: { uuid: string }): Promise<AxiosResponse<BasicResponse<Session>>> => {
    const response = await api.get(`/api/sessions/${uuid}`);
    return convertResponse(response, BasicResponse<Session>);
  },
  list: async ({
    rpp,
    page,
  }: {
    rpp: number;
    page: number;
  }): Promise<AxiosResponse<BasicResponse<Pagination<Session>>>> => {
    const response = await api.get('/api/sessions', {
      params: {
        rpp,
        page,
      },
    });
    return convertResponse(response, BasicResponse<Pagination<Session>>);
  },
};
