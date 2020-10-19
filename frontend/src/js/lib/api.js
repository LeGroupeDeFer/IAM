import { empty, identity, clean } from './utils';
import jwtDecode from 'jwt-decode';


/* istanbul ignore next */
const root = '/api';

/* istanbul ignore next */
const store = window.localStorage;

/* istanbul ignore next */
let currentAccessToken = null;

/* istanbul ignore next */
const encode = encodeURIComponent;

/* --------------------------------- Types --------------------------------- */

/**
 * A voucher of the user authorization. Claims are short lived and should be
 * renewed before their expiration.
 * 
 * @typedef {Object} Claim
 * 
 * @property {string} iss Issuer: The authority who issued the token.
 * @property {number} nbf Not Before: The time, in seconds since epoch, at
 *                        which this token becomes valid.
 * @property {number} exp Expiration: The time, in seconds since epoch, at
 *                        which this token becomes invalid.
 * @property {string} sub Subject: The subject for whom the token was issued.
 */

/**
 * TODO
 * 
 * @typedef {Object} CanData
 * 
 * @property {Date}   moment      The sample timestamp
 * @property {number} fillingRate The sample filling rate
 */

/**
 * TODO
 * 
 * @typedef {Object} Can
 * 
 * @property {number} id              The can id
 * @property {string} identifier      The can unique identifier
 * @property {number} latitude        The can position latitude
 * @property {number} longitude       The can position longitude
 * @property {Array<CanData>} [data]  The dated filling rate samples of this
 *                                    can
 */


/* --------------------------------- Core ---------------------------------- */

/**
 * TODO
 */
class APIError extends Error {
  
  constructor(code, message, data={}) {
    super(message);
    this.code = code;
    this.message = message;
    this.data = data;
  }

}


function query(target, search = {}) {
  const params = Object.keys(search)
    .map(key => search[key] instanceof Array
      ? `${key}=${search[key].map(encode).join(':')}`
      : `${key}=${encode(search[key])}`
    )
    .filter(identity);
  return empty(params) ? target : `${target}?${params.join('&')}`;
}


Object.assign(query, { encode });

/**
 * Requests a given backend API endpoint with the given configuration. This
 * function wraps the native window.fetch function with a set of sane,
 * json data oriented defaults.
 * 
 * @param {string}      endpoint  The request endpoint.
 * @param {RequestInit} config    The request configuration, may overwrite
 *                                defaults.
 * 
 * @returns {Promise<object|string>} The request result, extracted from the
 *                                   response.
 */
async function api(endpoint, { body, ...providedConfig } = {}) {
  const headers = { 'content-type': 'application/json' };

  if (currentAccessToken)
    headers['Authorization'] = `Bearer ${currentAccessToken}`;

  const method = providedConfig.method || (body ? 'POST' : 'GET');
  const config = {
    method,
    ...providedConfig,
    headers: {
      ...headers,
      ...providedConfig.headers,
    },
  };

  let target = `${root}${endpoint}`;
  if (body)
    if (method === 'GET') target = query(target, clean(body));
    else config.body = JSON.stringify(clean(body));

  return window
    .fetch(target, config)
    .then((response) =>
      Promise.all([
        new Promise((resolve, _) => resolve(response.status)),
        (response.headers.get('Content-Type') || '').includes('application/json')
          ? response.json()
          : response.text(),
      ])
    )
    .then(([status, data]) => {
      if (status < 200 || status >= 300)
        throw new APIError(status, data.reason, data);
      return data;
    });
}


/* --------------------------------- Auth ---------------------------------- */

/**
 * The auth object represents a dual token authentication mechanism. The first
 * token, the '''refresh token''', is delivered on login and is meant to be
 * long lived. Its sole purpose is to permit the request of a short lived
 * '''access token'''. The access token is a '''JSON Web Token''' claim, meant
 * to be used for resource protection and identity validation.
 * 
 * Calling this object by itself will delegate the call to the api on the auth
 * API segment.
 * 
 * @param {string}      endpoint The auth endpoint meant to be reached
 * @param {RequestInit} config   The request configuration
 * 
 * @returns {Promise<any>} The request response
 */
async function auth(endpoint, config={}) {
  return api(`/auth${endpoint}`, config);
}


Object.assign(auth, {
  
  /**
   * Clears all authentication state. This implies that API-wise, the user is
   * disconnected and his session is terminated. If authentication data is
   * preserved somewhere else, said data should be removed at the same time.
   */
  clear() {
    currentAccessToken = null;
    store.removeItem('__refresh_data__');
  },

  /**
   * Attempts to login the user with the given username and password. If the
   * authentication succeeds, this function will save the received refresh
   * token and attempt to retrieve an access token. Given that the refresh
   * lifetime is not known by this function, the backend may refuse the login
   * request if the user was already within a non-expired session.
   * 
   * @param {string} username   The username
   * @param {string} password   The user password
   * 
   * @returns {Promise<Claim>} The authentication voucher
   */
  async login(username, password) {
    return auth('/login', { body: { username, password } })
      .then(({ token }) => {
        store.setItem('__refresh_data__', `${username}:${token}`);
        return auth.refresh();
      });
  },

  /**
   * Attempts to logout the currently connected user. If the user is within an
   * expired session or not within a session at all, this function fails with a
   * 403 error code. In case of success, clears all authentication state.
   * 
   * @returns {Promise<void>}
   */
  async logout() {
    const refreshData = store.getItem('__refresh_data__') || '';
    const [username, token] = refreshData.split(':');

    if (token === null)
      throw new APIError(403, 'Not connected');

    await auth('/logout', { body: { username, token } });
    auth.clear();
  },

  /**
   * Attempts to retrieve an access token with the current refresh token. If
   * the user is within an expired session or not within a session at all, this
   * function fails with a 403 error code and clears all authentication state.
   * 
   * @returns {Promise<Token>} The authentication voucher
   */
  async refresh() {
    const refreshData = store.getItem('__refresh_data__') || '';
    const [username, token] = refreshData.split(':');

    if (!token)
      throw new APIError(403, 'Unable to find/parse local refresh token');

    try {
      const { access, refresh } = await auth(
        '/refresh',
        { body: { username, token } }
      );
      currentAccessToken = jwtDecode(access);
      store.setItem('__refresh_data__', `${username}:${refresh}`);
      return currentAccessToken;
    } catch (e) {
      if (e.code === 403) auth.clear();
      throw e;
    }

  },

  /**
   * Whether the auth currently posess a refresh token or not. In most
   * instances, this should be a direct representation of the session status.
   * However as the auth has no way to know of the refresh token lifetime, said
   * token may be expired and this function might return a wrong result.
   * 
   * @returns {boolean} true if the auth is within a session, false otherwise.
   */
  inSession() {
    return store.getItem('__refresh_data__') !== null;
  },

  /**
   * Whether a user is currently connected. Contrary to the inSession call,
   * this function always returns a correct result.
   * 
   * @returns {boolean} true if a user is connected, false otherwise.
   */
  connected() {
    if(currentAccessToken === null)
      return false;
    return currentAccessToken.exp > Date.now();
  }

});


/* ---------------------------------- Can ---------------------------------- */

/**
 * TODO
 * 
 * @param {string}      identifier The can identifier
 * @param {RequestInit} config TODO
 * 
 * @returns {Promise<Can>} TODO
 */
async function can(identifier, config={}) {
  return api(`/can/${identifier}`, config);
}


/* --------------------------------- Cans ---------------------------------- */

/**
 * TODO
 * 
 * @param {string} endpoint     TODO
 * @param {RequestInit} config  TODO
 * 
 * @returns {Promise<Array<Can>>} TODO
 */
async function cans(endpoint='', config={}) {
  return api(`/cans${endpoint}`, config);
}


Object.assign(cans, {
  
  /**
   * TODO
   * 
   * @returns {Promise<Can>} TODO
   */
  add() { /* TODO */ return Promise.reject('Not implemented'); },

  /**
   * TODO
   * 
   * @returns {Promise<void>} TODO
   */
  remove() { /* TODO */ return Promise.reject('Not implemented'); },

});


/* --------------------------------- Admin --------------------------------- */

/**
 * TODO
 * 
 * @param {string} endpoint TODO
 * @param {RequestInit} config TODO
 * 
 * @returns {Promise<object>} TODO
 */
async function admin(endpoint, config={}) {
  return api(`/admin${endpoint}`, config);
}


Object.assign(admin, {
  // TODO
});


/* ------------------------------------------------------------------------- */

Object.assign(api, {
  query,
  auth,
  can,
  cans,
  admin
});


export default api;
