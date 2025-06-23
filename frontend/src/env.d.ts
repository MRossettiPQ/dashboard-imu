declare namespace NodeJS {
  interface ProcessEnv {
    NODE_ENV: string;
    SERVER_API: string;
    VUE_ROUTER_MODE: 'hash' | 'history' | 'abstract' | undefined;
    VUE_ROUTER_BASE: string | undefined;
  }
}
