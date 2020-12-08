<script>
  import { createEventDispatcher } from 'svelte';
  import { Button } from 'sveltestrap';
  import canStore from 'iam/stores/can';
  import TextInput from 'iam/components/overall/TextEdit.svelte';

  export let can;

  // CHECK HERE
  $: copy = { ...can };

  const dispatch = createEventDispatcher();

  async function update() {
    try {
      await canStore.update(copy);
    } catch (error) {
      dispatch('error', { error });
    }
  }

  async function remove() {
    try {
      await canStore.remove(copy);
    } catch (error) {
      dispatch('error', { error });
    }
  }
</script>

<tr>
  <th scope="row">
    <TextInput on:blur={update} bind:text={copy.id} />
  </th>
  <td>
    <TextInput on:blur={update} bind:text={copy.longitude} />
  </td>
  <td>
    <TextInput on:blur={update} bind:text={copy.latitude} />
  </td>
  <td>
    <TextInput on:blur={update} bind:text={copy.signProtocol} />
  </td>
  <td>
    <Button outline color="danger" size="sm" on:click={remove}>Delete</Button>
  </td>
</tr>
