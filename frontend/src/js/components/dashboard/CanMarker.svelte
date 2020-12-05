<script>
  import { getContext, createEventDispatcher } from "svelte";
  import { mapbox, key } from "../../lib/mapbox.js";

  const { getMap } = getContext(key);
  const map = getMap();
  const dispatch = createEventDispatcher();

  export let can;

  // FIXME : Get color from CSS variables
  // primary, secondary, warning, danger
  const color = ["#546A7B", "#62929E", "#FFA62B", "#F71735"][
    Math.round(can.currentFill / 25.0)
  ];

  // TODO : update default marker with a can

  const marker = new mapbox.Marker({ color })
    .setLngLat([can.longitude, can.latitude])
    .addTo(map);

  marker
    .getElement()
    .addEventListener("click", () => dispatch("click", { can }));

  marker.getElement().style.cursor = "pointer";
</script>
