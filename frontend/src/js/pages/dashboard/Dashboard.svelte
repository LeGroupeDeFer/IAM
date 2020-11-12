<script>
  import Map from "../../components/dashboard/Map.svelte";
  import Popup from "../../components/dashboard/Popup.svelte";
  import MapMarker from "../../components/dashboard/MapMarker.svelte";

  let selectedCan;

  const cansMockUp = [
    {
      id: "Can id 1",
      longitude: 4.857599,
      latitude: 50.465856,
      publicKey: "ah oui oui oui",
    },
    {
      id: "Can id 2",
      longitude: 4.867699,
      latitude: 50.464846,
      publicKey: "string",
    },
  ];
  const cans = cansMockUp;
  // TODO : Implement connection with backend to get the list of cans
</script>

<style>
  main {
    display: table-row;
    height: 100%;
  }
</style>

<main>
  <Map lat={50.4667} lon={4.8667} zoom={14.5}>
    {#each cans as { id, latitude, longitude }}
      <MapMarker
        {latitude}
        {longitude}
        {id}
        on:click={(e) => (selectedCan = e.detail.id)} />
    {/each}
    <Popup bind:canID={selectedCan} />
  </Map>
</main>
