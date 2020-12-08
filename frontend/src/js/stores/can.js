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

  async function remove(can) {
    await api.admin.delete(can.id);
    update(state => ({
      ...state,
      focus: state.focus.id == can.id ? null : state.focus,
      cans: state.cans.filter(c => c.id != can.id)
    }));
  }

  async function updateCan(can) {
    await api.admin.update(
      can.id,
      copy.id,
      parseFloat(can.longitude),
      parseFloat(can.latitude),
      can.publicKey,
      can.signProtocol
    );
  
    update(state => ({
      ...state,
      cans: state.cans.map(c => c.id == can.id ? can : c)
    }));
  }

  async function add(can) {
    const { id, latitude, longitude, publicKey, signProtocol } = can;
    await api.admin.add(id, longitude, latitude, publicKey, signProtocol);
    
    update(state => ({
      ...state,
      cans: [...state.cans, can]
    }));
  }

  refresh();
  
  return { subscribe, refresh, focus, remove, update: updateCan, add };

}


const can = createCan();


export default can;
