<script>
  import Slider from "../../overall/Slider.svelte";

  export let fillValue = 0;
</script>

<style>
  div {
    width: 100%;
    text-align: center;
  }
</style>

<h6>How filled is your truck ?</h6>
<div>{fillValue}%</div>
<Slider bind:value={fillValue} />
