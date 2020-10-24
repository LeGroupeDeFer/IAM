<script>
  import { createEventDispatcher } from "svelte";
  import {
    Button,
    Modal,
    ModalBody,
    ModalFooter,
    ModalHeader,
    FormGroup,
    Input,
  } from "sveltestrap";

  export let isOpen = false;

  const dispatch = createEventDispatcher();
  const toggle = () => dispatch("toggle");

  let id = "";
  let longitude = "";
  let latitude = "";
  let publicKey = "";
  let fieldsCompleted = false;

  $: fieldsCompleted =
    id.length && longitude.length && latitude.length && publicKey.length;

  function clear() {
    id = "";
    longitude = "";
    latitude = "";
    publicKey = "";
  }
</script>

<style>
  .separation {
    padding: 0.5em;
  }
</style>

<Modal {isOpen} {toggle}>
  <ModalHeader {toggle}>Add a new can</ModalHeader>
  <ModalBody>
    <FormGroup>
      <Input type="text" placeholder="Enter the id" bind:value={id} />
      <span class="separation" />
      <Input
        type="text"
        placeholder="Enter the longitude"
        bind:value={longitude} />
      <span class="separation" />
      <Input
        type="text"
        placeholder="Enter the latitude"
        bind:value={latitude} />
      <span class="separation" />
      <Input
        type="text"
        placeholder="Enter the public key"
        bind:value={publicKey} />
    </FormGroup>
  </ModalBody>
  <ModalFooter>
    <Button
      color="primary"
      disabled={!fieldsCompleted}
      on:click={() => {
        dispatch('addToggled', { id, latitude, longitude, publicKey });
        toggle();
        clear();
      }}>
      Confirm
    </Button>
    <Button color="secondary" on:click={toggle}>Cancel</Button>
  </ModalFooter>
</Modal>
