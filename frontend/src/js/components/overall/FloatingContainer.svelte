<script>
  import { createEventDispatcher } from "svelte";
  import { fly } from "svelte/transition";
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
      max-height: 85%;
      overflow: scroll;
      /* Hide scrollbar for IE, Edge and Firefox */
      -ms-overflow-style: none; /* IE and Edge */
      scrollbar-width: none; /* Firefox */
    }
    /* Hide scrollbar for Chrome, Safari and Opera */
    .container::-webkit-scrollbar {
      display: none;
    }
  }
  /* Larger screens */
  @media (min-width: 900px) {
    .container {
      width: 25%;
      top: 10px;
      left: 10px;
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

<div class="container" transition:fly={{ y:50,  duration: 250 }}>
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
