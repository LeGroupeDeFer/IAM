<script>
  import auth from 'iam/stores/auth';
  
  export let reverse = false;
  $: predicate = reverse && !$auth || !reverse && $auth;
</script>

{#if predicate}
  <slot></slot>
{/if}
