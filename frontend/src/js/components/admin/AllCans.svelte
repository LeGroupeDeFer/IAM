<script>
  import Can from "./Can.svelte";
  import { Table } from "sveltestrap";
  import canStore from 'iam/stores/can';

  let cans = [];
  export let errors;

  canStore.subscribe(state => {
    cans = state.cans;
  });

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
        can={can}
        on:error={(e) => (errors = [...errors, e.detail.error])}
      />
    {/each}
  </tbody>
</Table>
