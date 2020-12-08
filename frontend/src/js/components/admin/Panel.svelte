<script>
  import canStore from 'iam/stores/can';
  import Notification from 'iam/components/overall/Notification.svelte';
  import AllCans from './AllCans.svelte';
  import AddCan from './AddCan.svelte';
  import { Spinner } from 'sveltestrap';

  let errors = [];

</script>

{#if !$canStore.pending}
  <AllCans bind:errors />
  <AddCan bind:errors />
  <Notification bind:messages={errors}>
    <span slot="header">Something went wrong...</span>
  </Notification>
{:else}
  <Spinner color="primary" />
{/if}
