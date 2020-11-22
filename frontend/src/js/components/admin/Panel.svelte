<script>
  import Notification from "../overall/Notification.svelte";
  import AllCans from "./AllCans.svelte";
  import AddCan from "./AddCan.svelte";
  import { onMount } from "svelte";
  import { Spinner } from "sveltestrap";
  import { api } from "../../lib";

  let cans;
  let errors = [];

  // Cannot use await then block :
  // Cannot bind to a variable declared with {#await ... then} or {:catch} blockssvelte(invalid-binding)
  onMount(async () => {
    try {
      cans = await api.cans.get();
    } catch (e) {
      errors = [...errors, networkError];
    }
  });
</script>

{#if cans}
  <AllCans bind:cans bind:errors />
  <AddCan bind:cans bind:errors />
  <Notification bind:messages={errors}>
    <span slot="header">Something went wrong...</span>
  </Notification>
{:else}
  <Spinner color="primary" />
{/if}
