<script>
  import { FormGroup, Input } from "sveltestrap";

  export let text = "";
  let toggled = false;
</script>

{#if !toggled}
  <span on:dblclick={() => toggled=true}><p>{text}</p></span>
{:else}
  <FormGroup>
    <Input
      type="text"
      placeholder="Enter the new name for the can"
      bind:value={text} 
      on:blur={() => toggled=false}/>
    </FormGroup>
{/if}
