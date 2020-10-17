import { empty, identity, clean } from './utils';


/* istanbul ignore next */
const root = '/api';

/* istanbul ignore next */
const store = window.localStorage;

/* istanbul ignore next */
let currentAccessToken;

/* istanbul ignore next */
const encode = encodeURIComponent;


/* --------------------------------- Core ---------------------------------- */

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


function api(endpoint, { body, ...providedConfig } = {}) {
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
        response.headers.get('Content-Type').includes('application/json')
          ? response.json()
          : response.text(),
      ])
    )
    .then(([status, data]) => {
      if (status < 200 || status >= 300) throw { ...data, code: status };
      return data;
    });
}


/* --------------------------------- Auth ---------------------------------- */

function auth(endpoint, config={}) {
  return api(`/auth${endpoint}`, config);
}


Object.assign(auth, {
  
  clear() {
    currentAccessToken = undefined;
    store.removeItem('__refresh_data__');
  },

  login(username, password) {
    auth.clear();
    return auth(
      '/login',
      { body: { username, password } }
    ).then(({ token }) => {
      store.setItem('__refresh_data__', `${username}:${token}`);
      return auth.refresh(username, token)
    });
  },

  logout() {
    const refreshData = store.getItem('__refresh_data__') || '';
    const [username, token] = refreshData.split(':');

    if (token === null)
      return Promise.reject({ code: 0, reason: 'Not connected' });

    return auth('/logout', { body: { username, token } }).then(
      data => auth.clear() || data
    );
  },

  refresh() {
    const refreshData = store.getItem('__refresh_data__') || '';
    const [username, token] = refreshData.split(':');

    if (!token) throw { code: 0, reason: 'Unable to find/parse local refresh token' };

    return auth('/refresh', { body: { username, token } })
      .then(({ access, refresh }) => {
        currentAccessToken = access;
        store.setItem('__refresh_data__', `${email}:${refresh}`);
        return { access, username };
      })
      .catch(({ code, reason }) => {
        if (code === 403)
          auth.clear();
        return Promise.reject({ code, reason });
      });
  },

  connected() {
    return store.getItem('__refresh_data__') !== null;
  },

});


/* --------------------------------- Cans ---------------------------------- */

function cans(endpoint='', config={}) {
  return api(`/cans${endpoint}`, config);
}


Object.assign(cans, {
  // TODO
});


/* --------------------------------- Admin --------------------------------- */

function admin(endpoint, config={}) {
  return api(`/admin${endpoint}`, config);
}


Object.assign(admin, {
  // TODO
});


/* ------------------------------------------------------------------------- */

Object.assign(api, {
  query,
  auth,
  cans,
  admin
});


export default api;
