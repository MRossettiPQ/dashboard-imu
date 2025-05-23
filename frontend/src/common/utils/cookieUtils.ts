export enum CookieType {
  ACCESS_TOKEN = 'ACCESS_TOKEN',
  ACCESS_TOKEN_DATE = 'ACCESS_TOKEN_DATE',
  REFRESH_TOKEN = 'REFRESH_TOKEN',
  REFRESH_TOKEN_DATE = 'REFRESH_TOKEN_DATE',
}

const isDevelopment = process.env.NODE_ENV === 'development';

interface CookieConfiguration {
  /**
   * Cookie expires detail; If specified as Number, then the unit is days; If specified as String, it can either be raw stringified date or in Xd Xh Xm Xs format (see examples)
   */
  expires?: number | string | Date;
  /**
   * Cookie path
   */
  path?: string;
  /**
   * Cookie domain
   */
  domain?: string;
  /**
   * SameSite cookie option
   */
  sameSite?: 'Lax' | 'Strict' | 'None';
  /**
   * Is cookie Http Only?
   */
  httpOnly?: boolean;
  /**
   * Is cookie secure? (https only)
   */
  secure?: boolean;
  /**
   * Raw string for other cookie options; To be used as a last resort for possible newer props that are currently not yet implemented in Quasar
   */
  other?: string;
}

export function getCookieConfiguration(expires: number | Date): CookieConfiguration {
  return {
    secure: !isDevelopment,
    expires: expires,
  };
}
