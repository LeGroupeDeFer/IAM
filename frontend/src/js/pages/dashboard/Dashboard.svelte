<script>
  import Navbar from "../../components/overall/Navbar.svelte";
  import Map from '../../components/dashboard/Map.svelte';
	import MapMarker from '../../components/dashboard/MapMarker.svelte';
</script>

<style>
  .mapbox {
    height: 75vh;
    margin: 1rem;
  }
</style>

<Navbar />
<div class='mapbox'>
  <Map lat={50.4667} lon={4.8667} zoom={14.5}>
    <MapMarker lat={50.465856} lon={4.857599} label="UNamur Eco-inf cans"/>
  </Map>
</div>
