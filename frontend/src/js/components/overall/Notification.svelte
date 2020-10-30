<script>
  import { Toast, ToastBody, ToastHeader } from "sveltestrap";

  export let isOpen;
</script>

<Toast {isOpen} class="mr-1">
  <ToastHeader>
    <slot name="header" />
  </ToastHeader>
  <ToastBody>
    <slot name="body" />
  </ToastBody>
</Toast>
