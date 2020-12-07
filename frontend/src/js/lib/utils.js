
/* --------------------------------- Core ---------------------------------- */

export const identity = x => x;


export const trace = x => console.log(x) || x;


/* ------------------------------ Array utils ------------------------------ */

export const empty = xs =>
  xs instanceof Array && xs.length === 0;


export const head = xs =>
  xs.length ? xs[0] : null;


export const last = xs =>
  xs && xs.length ? xs[xs.length - 1] : null;


export const tail = xs =>
  xs && xs.length ? xs.slice(1) : null;


export const map = f => xs => xs instanceof Array
  ? xs.map(f)
  : compose(fromEntries, map(([k, v]) => [k, f(v)]), entries)(xs);


export const filter = p => xs => xs instanceof Array
  ? xs.filter(p)
  : compose(fromEntries, filter(([_, v]) => p(v)), entries)(xs);


export const keys = o => Object.keys(o);


export const values = o => Object.values(o);


export const entries = o => Object.entries(o);


export const fromEntries = xs => Object.fromEntries(xs);


export const zip = (...xs) =>
  xs.length ? xs[0].map((_, i) => xs.map(e => e[i])) : [];


export const takeUntil = p => xs => {
  const taken = [];
  for (const x of xs) {
    if (p(x)) break;
    taken.push(x);
  }
  return taken;
}


export const groupBy = kf => xs => xs.reduce((a, x) => {
  const key = kf(x);
  return { ...a, [key]: [...(a[key] || []), x] };
}, {})


export const sum = xs => xs.reduce((x, a) => a + x, 0);


export const compose = (...fs) => o => fs.reduceRight((a, f) => f(a), o);


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


/* ------------------------------ Date utils ------------------------------- */

export const epoch = new Date(0);

export const now = () => Date.now() / 1000;

/* ------------------------------ Color utils ------------------------------ */

export function scaleColor(lower, upper, percent) {
  const [lo, hi] = [1.0 - percent, percent];
  
  var color = {
    red:   Math.floor(lower.red * lo + upper.red * hi),
    green: Math.floor(lower.green * lo + upper.green * hi),
    blue:  Math.floor(lower.blue * lo + upper.blue * hi)
  };
  return `rgb(${[color.red, color.green, color.blue].join(',')})`;
}

export const colorScales = {
  red: { red: 247, green: 23, blue: 53 },
  green: { red: 168, green: 219, blue: 168 }
};


/* ------------------------------------------------------------------------- */

export default {
  identity,
  
  empty,
  head,
  last,
  zip,
  takeUntil,
  groupBy,

  defined,
  truthy,
  update,
  clean,

  scaleColor,
  colorScales,

  epoch,
  now
};
