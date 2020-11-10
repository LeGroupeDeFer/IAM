<script>
  import { Toast, ToastBody, ToastHeader } from "sveltestrap";

  export let messages = [];

  let interval;

  $: {
    messages;
    window.clearInterval(interval);
    interval = setInterval(() => (messages = []), 5000);
  }
</script>

<style>
  .notification {
    position: fixed;
    z-index: 1000 !important;
    right: 0;
    top: 0;
    margin-top: 8px;
  }
</style>

<div class="notification">
  <Toast isOpen={messages.length > 0} class="mr-1">
    <ToastHeader>
      <slot name="header" />
    </ToastHeader>
    <ToastBody>
      {#each messages as message, index}
        <p>{message}</p>
        {#if !(messages.length == 1 || messages.length == index + 1)}
          <hr />
        {/if}
      {/each}
    </ToastBody>
  </Toast>
</div>
