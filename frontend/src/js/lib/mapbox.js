import mapbox from 'mapbox-gl';

// FIXME : Faudra que je pense à le revoke oups
mapbox.accessToken = "__MAPBOX_TOKEN__";

const api = "https://api.mapbox.com";

// points = [[longitude, latitude], ...]
async function direction(points) {
    const start = points.shift().join(',');
    const intermediates = points.map((point) => point.join(',')).join(';');
    const request =
        `${api}/optimized-trips/v1/mapbox/driving/` +
        `${start};${intermediates}` +
        `?geometries=geojson&access_token=${mapbox.accessToken}`;

    const response = await fetch(request);
    if (response.ok)
        return await response.json();
    else
        throw Error("Mapbox API did not respond correctly");
}

export { mapbox, direction };
