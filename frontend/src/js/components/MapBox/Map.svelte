<script>

  import { onMount } from 'svelte';
  import { Map, controls } from '@beyonk/svelte-mapbox';
  import Marker from './Marker.svelte';

  const { GeolocateControl, NavigationControl, ScaleControl } = controls;

  // State
  
  let map;

  onMount(() => {
    map.resize();
    map.setCenter([position.lon, position.lat], position.zoom);
  });

  // Props

  export let position = { lat: 50.4667, lon: 4.8667, zoom: 15 };
  export let markers = [];
  export const resize = () => map.resize();

</script>

<Map
  accessToken="__MAPBOX_TOKEN__"
  bind:this={map}
  style="mapbox://styles/mapbox/light-v10"
>
  <NavigationControl />
  <ScaleControl />
  {#each markers as marker}
    <Marker
      lat={marker.lat}
      lon={marker.lon}
      color={marker.color}
    />
  {/each}

  <slot></slot>
</Map>
