<script>
  import { Button } from "sveltestrap";
  import FillingInput from "./FillingInput.svelte";
  import PositionInput from "./PositionInput.svelte";

  let disabled;
  let fillValue;
  let longitude, latitude;

  $: disabled = !(longitude && latitude);

  function drawItinerary() {
    //TODO
    console.log("TODO");
  }
</script>

<style>
  .center {
    width: 100%;
    text-align: center;
  }
</style>

<PositionInput bind:latitude bind:longitude />
<hr />
<FillingInput {fillValue} />
<hr />
<div class="center">
  <Button {disabled} on:click={drawItinerary}>Calculate</Button>
</div>
