<script>
  import { getContext, onDestroy } from "svelte";
  import { key, direction } from "../../../lib/mapbox.js";
  import FillingInput from "./FillingInput.svelte";
  import PositionInput from "./PositionInput.svelte";

  export let cans;

  const { getMap } = getContext(key);
  const map = getMap();

  let fillValue;
  // Champion container park
  let latitude = 50.487769;
  let longitude = 4.904418;

  $: longitude, latitude, drawItinerary();

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
        map.getSource("route").setData(geojson);
      }
    } catch (error) {
      console.error(error);
      // TODO : display the Notification component
    }
  }

  function selectCans() {
    // Dumb algorithm :
    // a truck can have 10 cans in its container (small trcks you know)
    //
    // sort : We will sort the cans in descending order based on their filling
    //
    // map : If the truck has enough space, take it
    //       Else take as much as possible with the lesser filled cans
    selection = { ...cans };
    fillingAfter = fillValue * 10; // max is 10
    selection
      .sort((a, b) => b.currentFill - a.currentFill)
      .filter((can) => fillingAfter + can.currentFill <= 10);
    return cans;
  }

  onDestroy(() => {
    if (map.getSource("route")) {
      map.removeLayer("route");
      map.removeSource("route");
    }
  });
</script>

<PositionInput bind:latitude bind:longitude />
<hr />
<FillingInput {fillValue} />
