declare namespace NodeJS {
  interface ProcessEnv {
    ENV: string;
    NODE_ENV: string;
    SERVER_API: string;
    SOCKET_API: string;
    VUE_ROUTER_MODE: 'hash' | 'history' | 'abstract' | undefined;
    VUE_ROUTER_BASE: string | undefined;
  }
}
