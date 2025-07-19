export default {
  api: {
    // input: './openapi.yaml', // ou .json
    input: 'http://localhost:9000/q/openapi',
    output: {
      mode: 'single',
      target: './src/common/services/generated-api.ts',
      schemas: './src/common/services/generated-schemas',
      client: 'axios',
      override: {
        mutator: {
          path: './src/common/services/orval-axios.ts',
          name: 'axiosInstance',
        },
      },
    },
  },
};
