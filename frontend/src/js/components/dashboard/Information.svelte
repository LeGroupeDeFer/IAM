<script>
  import { createEventDispatcher } from "svelte";

  import {
    Card,
    CardBody,
    CardHeader,
    CardFooter,
    CardText,
    CardTitle,
    Button,
    Progress,
  } from "sveltestrap";

  export let can;

  let color = getColor(can.currentFill);
  let showStatistics = false;

  const dispatch = createEventDispatcher();

  function getColor(currentFill) {
    // FIXME : Get color from CSS variables
    return ["primary", "secondary", "warning", "danger"][
      Math.round(currentFill / 25.0)
    ];
  }

  function close() {
    dispatch("close", {});
  }

  $: can, (color = getColor(can.currentFill)), (showStatistics = false);
  // TODO : connect to backend
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
        TODO : check last time a filling rate was given so we can tell if there
        is a problem
        <hr />
        TODO : indicate the last time it was emptied
        <hr />
        <Button outline on:click={() => (showStatistics = !showStatistics)}>
          Statistics
        </Button>
      </CardText>
    </CardBody>
    <CardFooter>
      <Progress animated bar value={can.currentFill} {color}>
        {can.currentFill}
      </Progress>
    </CardFooter>
  </Card>
  {#if showStatistics}
    <Card body>TODO : showStatistics</Card>
  {/if}
</div>
