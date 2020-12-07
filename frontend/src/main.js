import App from './js/App.svelte';
import './scss/main.scss';

const app = new App({
	target: document.body,
	props: {
		version: '0.2.0'
	}
});


export default app;
