<script>
  
  import { afterUpdate } from 'svelte';
  import {
    Button,
    Card,
    CardBody,
    CardFooter,
    CardHeader,
    Spinner,
  } from 'sveltestrap';
  
  import canStore from 'iam/stores/can';
  import { Map, CanMarker } from 'iam/components/MapBox';
  import Sidebar from './Sidebar.svelte';

  // State

  let resizeMap = null;

  // Setup

  afterUpdate(() => resizeMap && resizeMap());

</script>

<div class="dashboard-map">
  {#if $canStore.pending}
  
    <div color="primary" class="abs-center">
      <Spinner type="grow" />
    </div>
  
  {:else}
    {#if !$canStore.error}
      
      <Map resize={resizeMap}>
        {#each $canStore.cans as can}
          <CanMarker can={can} />
        {/each}
      </Map>
      <Sidebar />

    {:else}
      
      <div class="abs-center">
        <Card>
          <CardHeader>Something went wrong</CardHeader>
          <CardBody>Server did not respond correctly...</CardBody>
          <CardFooter>
            <Button on:click={canStore.refresh}>Try again</Button>
          </CardFooter>
        </Card>
      </div>

    {/if}
  {/if}
</div>
