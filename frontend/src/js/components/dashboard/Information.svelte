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
    Alert,
  } from "sveltestrap";
  import { api } from "../../lib";

  export let can;

  let data = [];
  let alertColor = "secondary";
  let progressColor = getColor(can.currentFill);
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

  async function retrieveFillingRates(id) {
    // TODO : connect to backend
    // data = (await api.can(id)).data
    data = [
      {
        time: "1575909015200",
        fillingRate: 23,
      },
      {
        time: "1575909015300",
        fillingRate: 36,
      },
      {
        time: "1575909015400",
        fillingRate: 75,
      },
      {
        time: "1575909015500",
        fillingRate: 90,
      },
      {
        time: "1605815840000",
        fillingRate: 50,
      },
      {
        time: "1605815840200",
        fillingRate: 52,
      },
      {
        time: "1605815840900",
        fillingRate: 60,
      },
      {
        time: "1605815841000",
        fillingRate: 69,
      },
    ];
  }

  function getLastDump() {
    // FIXME: modify backend and openapi
    data.forEach((info) => (info.time = parseInt(info.time)));

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

  function getDaysSinceLastDump() {
    return (Date.now() - getLastDump().time) / (60 * 60 * 24 * 1000);
  }

  $: {
    can;
    showStatistics = false;
    progressColor = getColor(can.currentFill);
    retrieveFillingRates(can.id);
    if (getDaysSinceLastDump() > 5) alertColor = "danger";
    else if (getDaysSinceLastDump() > 2) alertColor = "danger";
    else alertColor = "success";
  }
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
        <Alert color={alertColor}>
          <h6>Last dumping :</h6>
          {new Date(getLastDump().time).toLocaleString()}
        </Alert>
        <hr />
        <Button outline on:click={() => (showStatistics = !showStatistics)}>
          Statistics
        </Button>
      </CardText>
    </CardBody>
    <CardFooter>
      <Progress animated bar value={can.currentFill} color={progressColor}>
        {can.currentFill}
      </Progress>
    </CardFooter>
  </Card>
  {#if showStatistics}
    <Card body>TODO : showStatistics</Card>
  {/if}
</div>
