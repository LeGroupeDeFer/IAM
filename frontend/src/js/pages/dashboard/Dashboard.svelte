<script>
  import Map from "../../components/dashboard/Map.svelte";
  import Information from "../../components/dashboard/Information.svelte";
  import CanMarker from "../../components/dashboard/CanMarker.svelte";
  import Notification from "../../components/overall/Notification.svelte";
  import { api } from "../../lib";
  import {
    Button,
    Card,
    CardBody,
    CardFooter,
    CardHeader,
    Spinner,
  } from "sveltestrap";

  let selectedCan;
  let cansRequest = api.cans.get();

  function getCans() {
    cansRequest = api.cans.get();
  }
  
  // TODO : Connect with backend when mockup is done
</script>

<style>
  main {
    display: table-row;
    height: 100%;
  }

  .center {
    position: absolute;
    left: 50%;
    top: 50%;
    -webkit-transform: translate(-50%, -50%);
    transform: translate(-50%, -50%);
  }
</style>

<main>
  <Map lat={50.4667} lon={4.8667} zoom={14.5}>
    {#await cansRequest}
      <div color="primary" class="center">
        <Spinner type="grow" />
      </div>
    {:then cans}
      {(cans = [{ id: 'Can id 1', longitude: 4.857599, latitude: 50.465856, publicKey: 'ah oui oui oui', currentFill: 35 }, { id: 'Can id 2', longitude: 4.867699, latitude: 50.464846, publicKey: 'string', currentFill: 84 }])}
      {#each cans as can}
        <CanMarker {can} on:click={(e) => (selectedCan = e.detail.can)} />
      {/each}
      {#if selectedCan}
        <Information
          bind:can={selectedCan}
          on:close={() => (selectedCan = undefined)} />
      {/if}
    {:catch error}
      <div class="center">
        <Card>
          <CardHeader>Something went wrong</CardHeader>
          <CardBody>Server did not respond correctly...</CardBody>
          <CardFooter>
            <Button on:click={getCans}>Try again</Button>
          </CardFooter>
        </Card>
      </div>
    {/await}
  </Map>
</main>
