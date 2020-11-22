<script>
  import Can from "./Can.svelte";
  import { Table } from "sveltestrap";
  import { api } from "../../lib";

  export let cans;
  export let errors;

  async function deleteCan(event) {
    try {
      await api.admin.delete(event.detail.id);
      cans = cans.filter((can) => can.id !== event.detail.id);
    } catch (error) {
      console.error(error);
      errors = [...errors, error.toString()];
    }
  }
</script>

<Table hover responsive>
  <thead>
    <tr>
      <th>ID</th>
      <th>Longitude</th>
      <th>Latitude</th>
      <th>Signing Protocol</th>
      <th />
    </tr>
  </thead>
  <tbody>
    {#each cans as can}
      <Can
        bind:can
        on:remove={(e) => deleteCan(e)}
        on:error={(e) => (errors = [...errors, e.detail.error])} />
    {/each}
  </tbody>
</Table>
