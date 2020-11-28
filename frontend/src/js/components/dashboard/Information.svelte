<script>
  import { createEventDispatcher } from "svelte";

  import {
    Card,
    CardBody,
    CardHeader,
    CardText,
    CardTitle,
    Spinner,
  } from "sveltestrap";
  import { api } from "../../lib";
  import FillingInfo from "./FillingInfo.svelte";
  import RequestsInfo from "./RequestsInfo.svelte";
  import DumpingInfo from "./DumpingInfo.svelte";

  export let can;

  let data;

  const dispatch = createEventDispatcher();

  function close() {
    dispatch("close", {});
  }

  async function retrieveFillingRates(id) {
    return await api.can(id);
  }

  $: can, (data = retrieveFillingRates(can.id));
</script>

<style>
  .container {
    position: absolute;
    z-index: 100 !important;
    max-height: 100%;
    margin: 0;
    border-radius: 3px;
  }
  /* Small screens */
  @media (max-width: 900px) {
    .container {
      width: auto;
      padding: 10px;
      top: 2px;
      right: 2px;
      left: 2px;
    }
  }
  /* Larger screens */
  @media (min-width: 900px) {
    .container {
      width: 25%;
      top: 10px;
      right: 10px;
    }
  }
  .close-button {
    position: absolute;
    padding: 10px;
    top: 0;
    right: 10px;
    cursor: pointer;
  }
  .center {
    position: relative;
    padding: 2rem;
    padding-left: 45%;
  }
</style>

<div class="container">
  <Card>
    <CardHeader>
      <CardTitle>
        <h5>{can.id}</h5>
      </CardTitle>
      <div class="close-button" on:click={close}><span>&times;</span></div>
    </CardHeader>
    <CardBody>
      <CardText>
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
      </CardText>
    </CardBody>
  </Card>
</div>
