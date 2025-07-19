import api from 'src/common/services/http-client';

export const axiosInstance = () => {
  return {
    instance: api,
  };
};
