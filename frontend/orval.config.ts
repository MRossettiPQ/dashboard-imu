import { defineConfig } from 'orval';

export default defineConfig({
  dashboardApi: {
    input: {
      target: 'http://localhost:8700/q/openapi',
    },
    output: {
      target: './src/common/api/generated/endpoints.ts',
      schemas: './src/common/api/generated/models',
      headers: true,
      client: 'axios',
      prettier: true,
      clean: true,
      // mode: 'tags',
      override: {
        useDates: true,
        mutator: {
          path: './src/common/api/mutator/custom-instance.ts', // Caminho para o arquivo acima
          name: 'customInstance',
        },
        enumGenerationType: 'enum',
      },
    },
  },
});
