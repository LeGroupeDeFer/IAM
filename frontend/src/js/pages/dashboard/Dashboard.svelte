<script>
  import Map from "../../components/dashboard/Map.svelte";
  import Information from "../../components/dashboard/Information.svelte";
  import CanMarker from "../../components/dashboard/CanMarker.svelte";

  let selectedCan;

  const cansMockUp = [
    {
      id: "Can id 1",
      longitude: 4.857599,
      latitude: 50.465856,
      publicKey: "ah oui oui oui",
      currentFill: 45,
    },
    {
      id: "Can id 2",
      longitude: 4.867699,
      latitude: 50.464846,
      publicKey: "string",
      currentFill: 78,
    },
    {
      id: "Can id 3",
      longitude: 4.853198,
      latitude: 50.462912,
      publicKey: "string",
      currentFill: 25,
    },
    {
      id: "Can id 4",
      longitude: 4.8610839,
      latitude: 50.4657973,
      publicKey: "string",
      currentFill: 10,
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
    {#each cans as can}
      <CanMarker {can} on:click={(e) => (selectedCan = e.detail.can)} />
    {/each}
    {#if selectedCan}
      <Information
        bind:can={selectedCan}
        on:close={() => (selectedCan = undefined)} />
    {/if}
  </Map>
</main>
