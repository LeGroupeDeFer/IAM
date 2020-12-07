<script>
	import { onMount, createEventDispatcher } from 'svelte';
	import { getContext } from 'svelte';
	import { MabBoxContext } from './context';

	const { getMap, getMapbox } = getContext(MabBoxContext);
	const map = getMap();
	const mapbox = getMapbox();
	
	// State

	let marker = null;
	let element = null;
	$: setClasses(className ? className.split(' ') : []);

	// Props

	export let lat;
	export let lon;
	export let name = null;
	export let popupClassName = 'beyonk-mapbox-popup';
	export let color = '#000';
	export let className = null;

	// Events
	const dispatch = createEventDispatcher();
	const onClick = _ => {
		dispatch('click', { lat, lon, name });
		map.flyTo({ center: [lon, lat] })
	}
	
	// Setup

	const setClasses = classes => {
		if (!(element && classes))
			return;
	
		const baseClasses = ['mapboxgl-marker', 'mapboxgl-marker-anchor-center'];
		const currentClasses = Array.from(element.classList);
		const targetedClasses = [...baseClasses, ...classes];
		
		for (const cls of currentClasses)
			if (!targetedClasses.includes(cls))
				element.classList.remove(cls);
		
		for (const cls of targetedClasses)
			if (!currentClasses.includes(cls))
				element.classList.add(cls);
	}

	onMount(() => {
	  marker = new mapbox.Marker({ color });
    marker.setLngLat([ lon, lat ]);

    if (name) {
      const popup = new mapbox.Popup({
        offset: 25,
        className: popupClassName
      })
      popup.setText(name);
      marker.setPopup(popup);
    }

		marker.addTo(map);
		element = marker.getElement();
		element.addEventListener('click', onClick);
		setClasses();

	  return () => marker.remove();
	});

</script>
