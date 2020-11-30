<script>
  import { getContext } from "svelte";
  import { key, direction } from "../../../lib/mapbox.js";
  import { Button } from "sveltestrap";
  import FillingInput from "./FillingInput.svelte";
  import PositionInput from "./PositionInput.svelte";

  export let cans;

  const { getMap } = getContext(key);
  const map = getMap();

  let disabled;
  let fillValue;
  let longitude, latitude;

  $: disabled = !(longitude && latitude);
  $: longitude, latitude, !disabled && drawItinerary();

  async function drawItinerary() {
    try {
      const destinations = [
        [longitude, latitude], // Initial position
        selectCans().map((can) => [can.longitude, can.latitude]),
      ];
      const answer = await direction(destinations);
      const geojson = {
        type: "Feature",
        properties: {},
        geometry: {
          type: "LineString",
          coordinates: answer.trips["0"].geometry.coordinates,
        },
      };

      if (map.getSource("route")) {
        map.getSource("route").setData(geojson);
      } else {
        map.addLayer({
          id: "route",
          type: "line",
          source: {
            type: "geojson",
            data: {
              type: "Feature",
              properties: {},
              geometry: {
                type: "LineString",
                coordinates: geojson,
              },
            },
          },
          paint: {
            "line-color": "#546A7B", // our primary color
            "line-width": 3,
            "line-opacity": 0.75,
          },
        });
      }
    } catch (error) {
      console.error(error);
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
