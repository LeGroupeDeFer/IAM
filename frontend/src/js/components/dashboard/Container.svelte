<script>
  import { createEventDispatcher } from "svelte";
  import { Card, CardBody, CardHeader, CardText, CardTitle } from "sveltestrap";

  const dispatch = createEventDispatcher();

  function close() {
    dispatch("close");
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
        <slot name="title" />
      </CardTitle>
      <div class="close-button" on:click={close}><span>&times;</span></div>
    </CardHeader>
    <CardBody>
      <CardText>
        <slot name="body" />
      </CardText>
    </CardBody>
  </Card>
</div>
