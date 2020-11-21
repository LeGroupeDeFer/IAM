<script>
  import { createEventDispatcher } from "svelte";

  import {
    Card,
    CardBody,
    CardHeader,
    CardText,
    CardTitle,
    Progress,
    Alert,
    Spinner,
    Button,
  } from "sveltestrap";
  import { api } from "../../lib";

  export let can;

  let data;

  const dispatch = createEventDispatcher();

  function close() {
    dispatch("close", {});
  }

  async function retrieveFillingRates(id) {
    return await api.can(id);
  }

  function getLastDump(data) {
    // FIXME: modify backend and openapi
    data.forEach((info) => (info.time = parseInt(info.time)));
    if (data.length < 1) return {};
    const lastDump = data
      .sort((a, b) => a.time - b.time)
      .reduce((acc, info) => {
        if (info.fillingRate < acc.lastFillingRate) {
          return {
            time: info.time,
            fillingRate: info.fillingRate,
            lastFillingRate: info.fillingRate,
          };
        } else {
          return { ...acc, lastFillingRate: info.fillingRate };
        }
      });
    return { time: lastDump.time, fillingRate: lastDump.fillingRate };
  }

  function getNumberOfRequest(data) {
    //TODO: implement
    return 78;
  }

  function getProgressColor(currentFill) {
    // FIXME: Get color from CSS variables
    return ["primary", "info", "warning", "danger"][
      Math.round(currentFill / 25.0)
    ];
  }

  function getDumpingColor(data) {
    const nbDays =
      (Date.now() - getLastDump(data).time) / (60 * 60 * 24 * 1000);
    if (nbDays > 5) return "danger";
    else if (nbDays > 2) return "warning";
    else return "info";
  }

  function getRequestColor(nbRequests) {
    // Cans are supposed to send their filling rate every 30 minutes
    // For the last two days, we're supposed to have 96 differents requests
    if (nbRequests >= 90) return "info";
    else if (nbRequests >= 60) return "warning";
    else return "danger";
  }

  // TODO: verify if connexion with backend really works
  // TODO: Refactor this wonderful piece of code 

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
          <Spinner type="grow" />
        {:then can}
          <Alert color={getDumpingColor(can.data)}>
            <h6>Last dumping :</h6>
            {new Date(getLastDump(can.data).time).toLocaleString()}
          </Alert>
          <hr />
          <Alert color={getRequestColor(getNumberOfRequest(can.data))}>
            <h6>Connexion state :</h6>
            {getNumberOfRequest(can.data)}/96 requests received over the last 2
            days
          </Alert>
        {:catch _}
          <h6>Something went wrong...</h6>
          <Button
            color="warning"
            on:click={() => (data = retrieveFillingRates(can.id))}>
            Try again
          </Button>
        {/await}
        <hr />
        <Alert color={getProgressColor(can.currentFill)}>
          <h6>Current filling :</h6>
          <Progress
            animated
            value={can.currentFill}
            color={getProgressColor(can.currentFill)}>
            {can.currentFill}%
          </Progress>
        </Alert>
      </CardText>
    </CardBody>
  </Card>
</div>
