<script>
  import AuthGuard from "../../components/auth/AuthGuard.svelte";
  import CanCell from "../../components/admin/CanCell.svelte";
  import NewCanModal from "../../components/admin/NewCanModal.svelte";
  import Notification from "../../components/overall/Notification.svelte";
  import { api } from "../../lib";
  import { Link } from "svelte-routing";
  import { Button, Table } from "sveltestrap";

  let errors = [];
  let timeout;
  let openedModal = false;
  const toggleModal = () => (openedModal = !openedModal);

  // TODO : Link with api
  let cans = [
    {
      id: "Can id 1",
      longitude: 4.857599,
      latitude: 50.465856,
      publicKey: "ah oui oui oui",
    },
    {
      id: "Can id 2",
      longitude: 4.827599,
      latitude: 50.445656,
      publicKey: "ah oui oui oui 2",
    },
  ];

  async function addCan(event) {
    const { id, latitude, longitude, publicKey } = event.detail;
    try {
      await api.admin.add(id, longitude, latitude, publicKey);
      cans = [...cans, { id, longitude, latitude, publicKey }];
    } catch (error) {
      console.error(error);
      errors = [...errors, error.toString()];
    }
  }

  async function deleteCan(event) {
    try {
      await api.admin.delete(event.detail.id);
      cans = cans.filter((can) => can.id !== event.detail.id);
    } catch (error) {
      console.error(error);
      errors = [...errors, error.toString()];
    }
  }

  $: errors,
    window.clearInterval(timeout),
    (timeout = setInterval(() => (errors = ""), 5000));
</script>

<style>
  .container {
    min-height: 75vh;
    padding-top: 5vh;
    vertical-align: middle;
  }
  .center {
    text-align: center;
  }
  .add-btn-container {
    position: fixed;
    bottom: 0;
    right: 0;
    padding-bottom: 5vh;
    padding-right: 5vw;
    text-align: end;
  }
</style>

<div class="container">
  <h1 class="text-primary">Administration panel</h1>
  <hr />
  <AuthGuard reverse>
    <p class="center">
      Please,
      <Link to="auth">authenticate</Link>
      yourself first.
    </p>
  </AuthGuard>

  <AuthGuard>
    <Table hover responsive>
      <thead>
        <tr>
          <th>ID</th>
          <th>Latitude</th>
          <th>Longitude</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {#each cans as can}
          <CanCell {can} on:remove={deleteCan} />
        {/each}
      </tbody>
    </Table>
    <div class="add-btn-container">
      <Button outline color="primary" size="lg" on:click={toggleModal}>
        Add a can
      </Button>
    </div>
  </AuthGuard>
</div>

<NewCanModal
  isOpen={openedModal}
  on:toggle={toggleModal}
  on:addToggled={addCan} />

<Notification isOpen={errors.length > 0}>
  <span slot="header">Something went wrong...</span>
  <div slot="body">
    {#each errors as error, index}
      <p>{error}</p>
      {#if !(errors.length == 1 || errors.length == index +1) }
        <hr />
      {/if}
    {/each}
  </div>
</Notification>
