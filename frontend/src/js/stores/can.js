import { writable } from 'svelte/store';
import { api, now, epoch } from '../lib';

class CanError extends Error {}


const refreshDelay = 30;


function createCan() {

  const { subscribe, update } = writable({
    cans:    [],
    focus:   null,
    updated: epoch,
    pending: true,
    error: null
  });

  let refreshTimeout = null;

  async function refresh() {
    try {
      clearTimeout(refreshTimeout);
      const cans = await api.cans();
      update(state => ({ ...state, cans, pending: false, updated: now() }));
      refreshTimeout = setTimeout(refresh, refreshDelay * 1000);
    } catch (e) {
      const caught = new CanError(e.message);
      update(state => ({ ...state, error: caught }))
      throw caught;
    }
  }

  async function focus(can) {
    if (can === null) {
      update(state => ({ ...state, focus: null }));
      return;
    }

    const focus = await api.can(can.id);
    focus.data = focus.data
      .map(s => ({ ...s, time: new Date(s.time) }))
      .sort((s1, s2) => s1.time - s2.time);
    
    update(state => ({ ...state, focus }));
  }

  refresh();
  
  return { subscribe, focus, refresh };

}


const can = createCan();


export default can;
