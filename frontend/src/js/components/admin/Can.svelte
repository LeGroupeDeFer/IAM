<script>
  import TextInput from "../overall/TextEdit.svelte";
  import { Button } from "sveltestrap";
  import { createEventDispatcher } from "svelte";
  import { api } from "../../lib";

  export let can;

  let copy = { ...can };

  const dispatch = createEventDispatcher();
  const remove = () => dispatch("remove", { id: can.id });

  async function updateCan() {
    try {
      await api.admin.update(
        can.id,
        copy.id,
        parseFloat(copy.longitude),
        parseFloat(copy.latitude),
        copy.publicKey,
        copy.signProtocol
      );
      can = { ...copy };
    } catch (error) {
      copy = { ...can };
      console.error(error);
      dispatch("error", { error });
    }
  }
</script>

<tr>
  <th scope="row">
    <TextInput on:blur={updateCan} bind:text={copy.id} />
  </th>
  <td>
    <TextInput on:blur={updateCan} bind:text={copy.longitude} />
  </td>
  <td>
    <TextInput on:blur={updateCan} bind:text={copy.latitude} />
  </td>
  <td>
    <TextInput on:blur={updateCan} bind:text={copy.signProtocol} />
  </td>
  <td>
    <Button outline color="danger" size="sm" on:click={remove}>Delete</Button>
  </td>
</tr>
