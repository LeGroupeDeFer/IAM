<script>
  import { createEventDispatcher } from "svelte";
  import { Spinner } from "sveltestrap";
  import { api } from "../../lib";
  import FillingInfo from "./CanInformation/FillingInfo.svelte";
  import RequestsInfo from "./CanInformation/RequestsInfo.svelte";
  import DumpingInfo from "./CanInformation/DumpingInfo.svelte";
  import Container from "../overall/Container.svelte";

  export let can;
  export let isOpen = false;

  let data;

  const dispatch = createEventDispatcher();

  async function retrieveFillingRates(id) {
    return await api.can(id);
  }

  function close() {
    isOpen = false;
    dispatch("close");
  }

  $: can, (isOpen = true), (data = retrieveFillingRates(can.id));
</script>

<style>
  .center {
    position: relative;
    padding: 2rem;
    padding-left: 45%;
  }
</style>

{#if isOpen}
  <Container on:close={close}>
    <h5 slot="title">{can ? can.id : 'tamer'}</h5>

    <div slot="body">
      {#await data}
        <div class="center">
          <Spinner type="grow" />
        </div>
      {:then can}
        <DumpingInfo {can} />
        <hr />
        <RequestsInfo {can} />
        <hr />
        <FillingInfo {can} />
      {/await}
    </div>
  </Container>
{/if}
