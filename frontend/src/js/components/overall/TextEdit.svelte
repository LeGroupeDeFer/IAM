<script>
  import { Input } from "sveltestrap";

  export let text = "";
  let toggled = false;
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
      on:blur={() => (toggled = false)} />
  </div>
{/if}
