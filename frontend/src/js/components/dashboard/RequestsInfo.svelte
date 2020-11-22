<script>
  import { Alert } from "sveltestrap";

  export let can;

  let color;
  let nbRequests;

  function getNumberOfRequests(data) {
    // TODO: debug once we have a working can
    const recent = data.filter(
      (info) => info.time > Date.now() - 24 * 60 * 60 * 1000 * 2
    );
    return recent.length;
  }

  function getColor(nbRequests) {
    // Cans are supposed to send their filling rate every 30 minutes
    // For the last two days, we're supposed to have 96 differents requests
    if (nbRequests >= 90) return "info";
    else if (nbRequests >= 60) return "warning";
    else return "danger";
  }

  $: can,
    (nbRequests = getNumberOfRequests(can.data)),
    (color = getColor(nbRequests));
</script>

<Alert {color}>
  <h6>Connexion state :</h6>
  {nbRequests}/96 requests received over the last 2 days
</Alert>
