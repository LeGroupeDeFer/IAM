<script>
  import Login from "./pages/auth/Login.svelte";
  import { Router, Route } from "svelte-routing";

  export let url;
  export let version;
</script>

<Router {url}>
  <Route path="/"><Login {version} /></Route>
</Router>
