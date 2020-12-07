import { map, filter, groupBy, sum, compose, tail, head } from './utils';


const afterDate =
  date => filter(s => s.time > date);


const beforeDate =
  date => filter(s => s.time < date);


const byHour =
  groupBy(sample => sample.time.getHours());


const byDate =
  groupBy(sample => `${sample.time.getDate()}/${sample.time.getMonth()}`);


const withoutGaps =
  labels => samples => labels.reduce(
    (a, l) => ({ ...a, [l]: (samples[l] ? samples[l] : []) }),
    {}
  );


const fillingRates =
  map(samples => samples.map(sample => sample.fillingRate));


const averaged =
  map(samples => samples.length == 0 ? 0 : sum(samples) / samples.length);


export function lastDayActivity(can) {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);

  let labels = Array(24)
    .fill()
    .map((_, i) => (yesterday.getHours() + i) % 24);
  labels = [...tail(labels), head(labels)];

  const pastDay = afterDate(yesterday);
  const fullDay = withoutGaps(labels);
  const f = compose(averaged, fillingRates, fullDay, byHour, pastDay);
  const dataset = f(can.data);

  return {
    labels: labels.map(l => `${l}h`),
    datasets: [ { values: labels.map(l => dataset[l]) }],
    yMarkers: [
      {
          label: '',
          value: 100,
          type: 'solid'
      }
    ]
  };
}

export function lastWeekActivity(can) {
  const yesterweek = new Date();
  yesterweek.setDate(yesterweek.getDate() - 6);
  
  const labels = Array(7)
    .fill()
    .map((_, i) => {
      const day = new Date(yesterweek);
      day.setDate(day.getDate() + i);
      return `${day.getDate()}/${day.getMonth()}`;
    });

  const pastWeek = afterDate(yesterweek);
  const fullWeek = withoutGaps(labels);
  const f = compose(averaged, fillingRates, fullWeek, byDate, pastWeek);
  const dataset = f(can.data);

  return {
    labels: labels,
    datasets: [ { values: labels.map(l => dataset[l]) } ],
    yMarkers: [
      {
          label: '',
          value: 100,
          type: 'solid'
      }
    ]
  };
}
