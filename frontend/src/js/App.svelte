<script>
	import Login from './components/auth/Login.svelte';
	export let version;
</script>

<main>
	<div class="content text-center">
		<p class="text-muted my-0">v{version}</p>
		<h1 class="text-primary py-0 my-0">IAM</h1>
		<a href="https://github.com/LeGroupeDeFer/IAM" class="git-link">
			View on HitHub
		</a>

		<hr />
		<Login />
	</div>
</main>

<style>
	.content {
		width: 300px;
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
	}

	h1 {
		font-size: 5em;
	}

	.git-link {
		color: #462521;
	}

	.git-link:hover {
		text-decoration: none;
		border-bottom: 3px solid #462521;
		padding-bottom: 3px;
	}
</style>