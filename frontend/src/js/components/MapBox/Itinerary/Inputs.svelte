<script>
  import { getContext, onDestroy } from 'svelte';
  import { MapBoxContext } from '../context';
  import { direction } from 'iam/lib/mapbox.js';
  
  import FillingInput from './FillingInput.svelte';
  import PositionInput from './PositionInput.svelte';


  // Props

  export let cans;

  // State
  
  const c = getContext(MapBoxContext);
	const map = c.getMap && c.getMap();

  let fillValue;
  
  // Champion container park
  let latitude = 50.487769;
  let longitude = 4.904418;

  $: longitude, latitude, fillValue, handleItinerary();

  async function handleItinerary() {
    try {
      const destinations = [
        [longitude, latitude], // Initial position
        ...selectCans().map((can) => [can.longitude, can.latitude]),
      ];

      if (destinations.length < 2) {
        draw(getGeojson([]));
        throw "No can can be charged into your truck...";
      }

      const answer = await direction(destinations);
      const geojson = getGeojson(answer.trips["0"].geometry.coordinates);
      draw(geojson);
    } catch (error) {
      console.log(error);
      // TODO : display the Notification component
    }
  }

  function getGeojson(coord) {
    return {
      type: "Feature",
      properties: {},
      geometry: {
        type: "LineString",
        coordinates: coord,
      },
    };
  }

  function draw(geojson) {
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
  }

  function selectCans() {
    let actualFill = fillValue / 10.0 ?? 0.0;
    return cans
      .sort((a, b) => b.currentFill - a.currentFill)
      .filter((can) => {
        if (actualFill + can.currentFill / 100.0 <= 10) {
          actualFill += can.currentFill / 100.0;
          return true;
        } else return false;
      });
  }

  onDestroy(() => {
    if (map.getSource('route')) {
      map.removeLayer('route');
      map.removeSource('route');
    }
  });
</script>

<PositionInput bind:latitude bind:longitude />
<hr />
<FillingInput bind:fillValue />
