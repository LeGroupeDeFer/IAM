<script>
  import {
    Button,
    Modal,
    ModalBody,
    ModalFooter,
    ModalHeader,
    FormGroup,
    Input,
  } from "sveltestrap";

  export let open = false;
  export let toggle;
  export let addCan;

  let id = "";
  let longitude = "";
  let latitude = "";
  let publicKey = "";

  function clear() {
    id = "";
    longitude = "";
    latitude = "";
    publicKey = "";
  }
</script>

<Modal isOpen={open} {toggle}>
  <ModalHeader {toggle}>Add a new can</ModalHeader>
  <ModalBody>
    <FormGroup>
      <Input type="text" placeholder="Enter the id" bind:value={id} />
      <Input
        type="text"
        placeholder="Enter the longitude"
        bind:value={longitude} />
      <Input
        type="text"
        placeholder="Enter the latitude"
        bind:value={latitude} />
      <Input
        type="text"
        placeholder="Enter the public key"
        bind:value={publicKey} />
    </FormGroup>
  </ModalBody>
  <ModalFooter>
    <Button
      color="primary"
      on:click={() => {
        addCan(id, latitude, longitude, publicKey);
        toggle();
        clear();
      }}>
      Confirm
    </Button>
    <Button color="secondary" on:click={toggle}>Cancel</Button>
  </ModalFooter>
</Modal>
