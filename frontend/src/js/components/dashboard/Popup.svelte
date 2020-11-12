<script>
  import {
    Card,
    CardBody,
    CardHeader,
    CardFooter,
    CardText,
    CardTitle,
    Progress,
  } from "sveltestrap";

  export let canID;

  // TODO : connect to backend
  let progress;
  let color;

  // TODO : temporary
  $: canID, (progress = Math.floor(Math.random() * Math.floor(100)));

  $: if (progress > 85) {
    color = "danger";
  } else if (progress > 60) {
    color = "warning";
  } else {
    color = "primary";
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

{#if canID}
  <div class="container">
    <Card>
      <CardHeader>
        <CardTitle>
          <h5>{canID}</h5>
        </CardTitle>
        <div class="close-button" on:click={() => (canID = null)}>
          <span>&times;</span>
        </div>
      </CardHeader>
      <CardBody>
        <CardText>
          Sooo, it'd be great to add some info, but I don't know what for now.
        </CardText>
      </CardBody>
      <CardFooter>
        <Progress animated bar value={progress} {color}>{progress}</Progress>
      </CardFooter>
    </Card>
  </div>
{/if}
