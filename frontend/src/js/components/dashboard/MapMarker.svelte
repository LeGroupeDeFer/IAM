<script>
  import { getContext, createEventDispatcher } from "svelte";
  import { mapbox, key } from "../../lib/mapbox.js";

  const { getMap } = getContext(key);
  const map = getMap();
  const dispatch = createEventDispatcher();

  export let latitude;
  export let longitude;
  export let id;

  const marker = new mapbox.Marker()
    .setLngLat([longitude, latitude])
    .addTo(map);

  marker
    .getElement()
    .addEventListener("click", () => dispatch("click", { id }));

  // FIXME : no working
  marker.on("mousehover", () => dispatch("click", { id }));
</script>
