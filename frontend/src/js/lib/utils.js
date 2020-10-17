
/* --------------------------------- Core ---------------------------------- */

export const identity = x => x;


/* ------------------------------ Array utils ------------------------------ */

export const empty = xs =>
  xs instanceof Array && xs.length === 0;


export const head = xs =>
  xs.length ? xs[0] : null;


export const last = xs =>
  xs && xs.length ? xs[xs.length - 1] : null;


export const zip = (...xs) =>
  xs.length ? xs[0].map((_, i) => xs.map(e => e[i])) : [];


/* ----------------------------- Object utils ------------------------------ */

export const defined = thing =>
  thing !== undefined && thing !== null;


export const truthy = thing =>
  thing instanceof Array ? thing.length : thing;


export const update = (o, k, v) =>
  ({ ...o, [k]: v });


export const clean = (o, hard = false) => {
  const predicate = hard ? truthy : defined;
  return Object
    .keys(o)
    .reduce((a, k) => predicate(o[k]) ? update(a, k, o[k]) : a, {});
}

/* ------------------------------------------------------------------------- */

export default {
  identity,
  
  empty,
  head,
  last,
  zip,

  defined,
  truthy,
  update,
  clean
};
