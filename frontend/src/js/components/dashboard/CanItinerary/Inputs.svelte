<script>
  import { Button } from "sveltestrap";
  import FillingInput from "./FillingInput.svelte";
  import PositionInput from "./PositionInput.svelte";

  function drawItinerary() {
    console.log("TODO")
  }
</script>

<style>
  .center {
    width: 100%;
    text-align: center;
  }
</style>

<PositionInput />
<hr />
<FillingInput />
<hr />
<div class="center">
  <Button on:click={drawItinerary}>Calculate</Button>
</div>
