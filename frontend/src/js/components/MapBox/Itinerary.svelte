<script>
  
  import { fly } from 'svelte/transition';
  import { Button } from 'sveltestrap';
  import FloatingContainer from 'iam/components/overall/FloatingContainer.svelte';
  import Inputs from './Itinerary/Inputs.svelte';
  import canStore from 'iam/stores/can';
  
  let cans = [];
  let isOpen = false;

  canStore.subscribe(state => cans = state.cans);

</script>

<style>
  .bottom {
    position: absolute;
    z-index: 1000;

    left: 50%;
    bottom: 10px;

    transform: translateX(-50%);

    text-align: center;
  }
</style>

{#if isOpen}

  <FloatingContainer on:close={() => (isOpen = false)}>
    <h5 slot="title">Calculate an itinerary</h5>
    <div slot="body">
      <Inputs {cans} />
    </div>
  </FloatingContainer>

{:else}

  <div transition:fly={{ y: 50, duration: 250 }} class="bottom">
    <Button on:click={() => (isOpen = true)}>Calculate an itinerary</Button>
  </div>

{/if}
