<script>
  import { Alert } from "sveltestrap";
  import { last } from "../../lib";

  export let can;

  let color;
  let date;

  function getLastDump(data) {
    // FIXME: modify backend and openapi
    data.forEach((info) => (info.time = parseInt(info.time)));
    if (data.length < 1) return {};
    const lastDump = data
      .sort((a, b) => a.time - b.time)
      .reduce((acc, info) => {
        if (info.fillingRate < acc.lastFillingRate) {
          return {
            time: info.time,
            fillingRate: info.fillingRate,
            lastFillingRate: info.fillingRate,
          };
        } else {
          return { ...acc, lastFillingRate: info.fillingRate };
        }
      });
    return { time: lastDump.time, fillingRate: lastDump.fillingRate };
  }

  function getColor(data) {
    const nbDays =
      (Date.now() - getLastDump(data).time) / (60 * 60 * 24 * 1000);

    if (nbDays > 5) return "danger";
    else if (nbDays > 2) return "warning";
    else return "info";
  }

  function getDate(data) {
    const lastDump = getLastDump(data);
    const date = lastDump.time
      ? new Date(lastDump.time).toLocaleString()
      : "No data available.";
    return date;
  }

  $: can, (color = getColor(can.data)), (date = getDate(can.data));
</script>

<Alert {color}>
  <h6>Last dumping :</h6>
  {date}
</Alert>
