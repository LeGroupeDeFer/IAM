<script>
  import { fly } from "svelte/transition";
  import { Button } from "sveltestrap";
  import Container from "../overall/Container.svelte";
  import Inputs from "./CanItinerary/Inputs.svelte";
  
  export let cans;
  export let isOpen = false;
</script>

<style>
  .bottom {
    position: absolute;
    z-index: 1000;
    bottom: 10px;
    width: 100%;
    text-align: center;
  }
</style>

{#if isOpen}
  <Container on:close={() => (isOpen = false)}>
    <h5 slot="title">Calculate an itinerary</h5>
    <div slot="body">
      <Inputs {cans} />
    </div>
  </Container>
{:else}
  <div transition:fly={{ y: 50, duration: 250 }} class="bottom">
    <Button on:click={() => (isOpen = true)}>Calculate an itinerary</Button>
  </div>
{/if}
