<script>
  import { getContext, onDestroy } from "svelte";
  import { mapbox, key } from "../../../lib/mapbox.js";

  export let longitude, latitude;

  const { getMap } = getContext(key);
  const map = getMap();

  let marker;

  function placeMarker(e) {
    longitude = e.lngLat.lng;
    latitude = e.lngLat.lat;
    marker && marker.remove();
    marker = new mapbox.Marker({ color: "#BDE4A7" })
      .setLngLat([longitude, latitude])
      .addTo(map);
  }

  map.on("click", placeMarker);

  onDestroy(() => {
    map.off("click", placeMarker);
    marker && marker.remove();
  });
</script>

<h6>Where are you ?</h6>
{#if longitude && latitude}
  <span>Longitude: {longitude.toFixed(4)}</span>
  <span>Latitude: {latitude.toFixed(4)}</span>
{:else}<span>Click on your position relative to the map.</span>{/if}
