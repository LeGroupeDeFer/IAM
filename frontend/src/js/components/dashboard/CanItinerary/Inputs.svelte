<script>
  import { direction } from "../../../lib/mapbox.js";
  import { Button } from "sveltestrap";
  import FillingInput from "./FillingInput.svelte";
  import PositionInput from "./PositionInput.svelte";

  export let cans;

  let disabled;
  let fillValue;
  let longitude, latitude;

  $: disabled = !(longitude && latitude);

  async function drawItinerary() {
    try {
      const destinations = [
        [longitude, latitude], // Initial position
        selectCans().map((can) => [can.longitude, can.latitude]),
      ];
      const answer = await direction(destinations);
      console.log(answer);
      // TODO : do something with the response
    } catch (error) {
      console.error(error)
      // TODO : display the Notification component
    }
  }

  function selectCans() {
    //TODO : Make a selection considering the truck current filling
    return cans;
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
