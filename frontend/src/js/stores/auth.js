import { writable } from 'svelte/store';
import { api, now } from '../lib';

class AuthError extends Error {}


/*
 * Time in seconds before token expiration at which we should ask for a new
 * token.
 */
const refreshDelay = 30;


function createAuth() {

  const { subscribe, set } = writable(null);
  let refreshTimeout = null;

  async function login(username, password) {
    if (api.auth.inSession())
      throw new AuthError('AuthContext.login: Already connected');

    const token = await api.auth.login(username, password);
    set(token);
    
    refreshTimeout = setTimeout(refresh, (token.exp - now() - refreshDelay) * 1000);
  }

  async function logout() {
    if (!api.auth.inSession())
      throw new AuthError('AuthContext.logout: Not connected');

    await api.auth.logout();
    set(null);

    clearTimeout(refreshTimeout);
  }

  async function refresh() {
    if (!api.auth.inSession())
      throw new AuthError('AuthContext.refresh: Not connected');
    
    try {
      const token = await api.auth.refresh();
      set(token);
      setTimeout(refresh, (token.exp - now() - refreshDelay) * 1000);
    } catch (e) {
      api.auth.clear();
      throw e;
    }
  }

  // If we're in a session we can try to refresh
  if (api.auth.inSession()) refresh();

  return { subscribe, login, logout };

}


const auth = createAuth();


export default auth;
