<script>
    export let color = "success";
</script>

<div class={`alert alert-${color}`} role="alert">
    <slot />
</div>
