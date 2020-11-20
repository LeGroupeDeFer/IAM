<script>
  import NewCanModal from "./NewCanModal.svelte";
  import { Button } from "sveltestrap";
  import { api } from "../../lib";

  export let cans;
  export let errors;

  let openedModal = false;

  const toggleModal = () => (openedModal = !openedModal);

  async function addCan(event) {
    const { id, latitude, longitude, publicKey, signProtocol } = event.detail;
    try {
      await api.admin.add(id, longitude, latitude, publicKey, signProtocol);
      cans = [...cans, { id, latitude, longitude, publicKey, signProtocol }];
    } catch (error) {
      console.error(error);
      errors = [...errors, error.toString()];
    }
  }
</script>

<style>
  .add-btn-container {
    position: fixed;
    bottom: 0;
    right: 0;
    padding-bottom: 5vh;
    padding-right: 5vw;
    text-align: end;
  }
</style>

<div class="add-btn-container">
  <Button outline color="primary" size="lg" on:click={toggleModal}>
    Add a can
  </Button>
</div>

<NewCanModal
  isOpen={openedModal}
  on:toggle={toggleModal}
  on:addToggled={(e) => addCan(e)} />
