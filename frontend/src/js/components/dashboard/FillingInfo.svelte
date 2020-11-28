<script>
  import { Progress } from "sveltestrap";
  import Alert from "./CustomAlert.svelte";
  
  export let can;

  let color;

  function getColor(currentFill) {
    return ["primary", "info", "warning", "danger"][
      Math.round(currentFill / 25.0)
    ];
  }

  $: can, (color = getColor(can.currentFill));
</script>

<Alert {color}>
  <h6>Current filling :</h6>
  <Progress animated value={can.currentFill} {color}>
    {can.currentFill}%
  </Progress>
</Alert>
