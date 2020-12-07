<script>
  
  import Icon from 'svelte-awesome';
  import { timesCircle } from 'svelte-awesome/icons';
  import { Spinner } from 'sveltestrap';
  
  import canStore from 'iam/stores/can';
  import CanSummary from 'iam/components/can/Summary.svelte';

  // State

  $: shift = Boolean($canStore.focus);
  $: active = $canStore.focus !== null;

  // Events

  const onClose = _ => canStore.focus(null);

</script>

<div
  class="dashboard-sidebar"
  class:active={$canStore.focus}
  class:shift={shift}
>
  {#if active}

    <div class="sidebar-toggle">
      <h2>{$canStore.focus.id}</h2>
      <button on:click|preventDefault={onClose}>
        <Icon data={timesCircle} scale="2" />
      </button>
    </div>
    
    <hr />

    {#if $canStore.pending}
      <Spinner type="grow" />
    {:else} 
      <CanSummary />
    {/if}
    
  {/if}
</div>
