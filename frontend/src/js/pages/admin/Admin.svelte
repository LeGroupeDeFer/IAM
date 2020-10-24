<script>
  import AuthGuard from "../../components/auth/AuthGuard.svelte";
  import Can from "../../components/admin/Can.svelte";
  import NewCanModal from "../../components/admin/NewCanModal.svelte";
  import { Link } from "svelte-routing";
  import { Button } from "sveltestrap";

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

  function addCan(event) {
    // api.can.add(newCan)
    const { id, latitude, longitude, publicKey } = event.detail;
    const newCan = { id, longitude, latitude, publicKey };
    cans = [...cans, newCan];
  }

  function deleteCan(event) {
    // api.can.remove(id) TODO
    cans = cans.filter((can) => can.id !== event.detail.id);
  }
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

  .can {
    padding-bottom: 0.3em;
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
  <AuthGuard reverse>
    <p class="center">
      Please,
      <Link to="auth">authenticate</Link>
      yourself first.
    </p>
  </AuthGuard>

  <AuthGuard>
    {#each cans as can}
      <div class="can">
        <Can {can} on:deleteToggled={deleteCan} />
      </div>
    {/each}

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
