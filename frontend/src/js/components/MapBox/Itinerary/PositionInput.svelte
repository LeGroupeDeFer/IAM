<script>
  
  import { getContext, onDestroy } from 'svelte';
  import { MapBoxContext } from '../context';

  // Props

  export let longitude, latitude;

  // State

  const { getMap, getMapbox } = getContext(MapBoxContext);
  const map = getMap();
  const mapbox = getMapbox();
  
  let marker;

  $: longitude, latitude, placeMarker();


  function placeMarker() {
    marker && marker.remove();
    marker = new mapbox.Marker({ color: "#BDE4A7" })
      .setLngLat([longitude, latitude])
      .addTo(map);
    marker.getElement().classList.add('itinerary-marker');
  }

  // Events

  map.on("click", (e) => {
    longitude = e.lngLat.lng;
    latitude = e.lngLat.lat;
  });

  // Setup

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
