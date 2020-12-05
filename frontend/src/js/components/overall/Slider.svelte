<script>
  export let value = 0;
  export let min = 0;
  export let max = 100;
</script>

<style>
  div {
    width: 100%;
  }
  input {
    width: 100% !important;
  }
</style>

<div>
  <input type="range" {min} {max} bind:value />
</div>
