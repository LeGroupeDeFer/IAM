<script>
  import { Button } from 'sveltestrap';
  import canStore from 'iam/stores/can';
  import NewCanModal from './NewCanModal.svelte';

  export let errors;

  let openedModal = false;

  const toggleModal = () => (openedModal = !openedModal);

  async function addCan(event) {
    try {
      canStore.add(event.detail);
    } catch (error) {
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
  on:addToggled={addCan}
/>
