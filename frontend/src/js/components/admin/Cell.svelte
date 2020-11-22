<script>
  import { Button } from "sveltestrap";
  import { createEventDispatcher } from "svelte";
  import TextInput from "../overall/TextEdit.svelte";

  export let can;

  const dispatch = createEventDispatcher();
  const remove = () => dispatch("remove", { id: can.id });

  // TODO : Connect to api for textInput
</script>

<tr>
  <th scope="row">
    <TextInput text={can.id} />
  </th>
  <td>
    <TextInput text={can.latitude} />
  </td>
  <td>
    <TextInput text={can.longitude} />
  </td>
  <td>
    <TextInput text={can.signProtocol} />
  </td>
  <td>
    <Button outline color="danger" size="sm" on:click={remove}>Delete</Button>
  </td>
</tr>
