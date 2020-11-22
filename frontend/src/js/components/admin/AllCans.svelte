<script>
  import Cell from "./Cell.svelte";
  import { Table } from "sveltestrap";
  import { api } from "../../lib";

  export let cans;
  export let errors;

  async function deleteCan(event) {
    try {
      console.log(event.detail.id)
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
      <th>Latitude</th>
      <th>Longitude</th>
      <th>Signing Protocol</th>
      <th />
    </tr>
  </thead>
  <tbody>
    {#each cans as can}
      <Cell {can} on:remove={(e) => deleteCan(e)} />
    {/each}
  </tbody>
</Table>
