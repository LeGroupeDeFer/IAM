<script>

  import canStore from 'iam/stores/can';
  import Marker from './Marker.svelte';
  import { scaleColor, colorScales } from 'iam/lib';
  
  // Props

  export let can;

  // State

  $: color = scaleColor(
    colorScales.green,
    colorScales.red,
    can.currentFill / 100
  );

  $: focusPredicate = $canStore.focus && $canStore.focus.id == can.id;
  $: className = focusPredicate && 'active' || '';

  // Events

  const onClick = e => {
    e.preventDefault();
    e.stopPropagation();
    canStore.focus(can);
  }

</script>

<Marker
  lat={can.latitude}
  lon={can.longitude}
  color={color}
  className={className}
  on:click={onClick}
/>
