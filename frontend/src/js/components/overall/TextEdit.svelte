<script>
  import { createEventDispatcher } from "svelte";
  import { Input } from "sveltestrap";

  export let text = "";

  let toggled = false;
  let dispatch = createEventDispatcher();
</script>

<style>
  div {
    width: fit-content;
  }
</style>

{#if !toggled}
  <div on:dblclick={() => (toggled = true)}>
    <p>{text}</p>
  </div>
{:else}
  <div>
    <Input
      type="text"
      placeholder="Enter the new name for the can"
      bind:value={text}
      on:blur={() => {
        toggled = false;
        dispatch('blur');
      }} />
  </div>
{/if}
