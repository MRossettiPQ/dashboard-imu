import { fileURLToPath } from 'url';
import path from 'path';
import { config as dotenvConfig } from 'dotenv';

export default (() => {
  const __dirname = path.dirname(fileURLToPath(import.meta.url));

  let fileEnvName = '.env';
  if (process.env.NODE_ENV) {
    fileEnvName = `${fileEnvName}.${process.env.NODE_ENV}`;
  }

  const envPath = path.resolve(__dirname, `./${fileEnvName}`);
  const env = dotenvConfig({ path: envPath }).parsed;
  console.log(env);
  return env;
})();
