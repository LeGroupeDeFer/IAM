<script>
  import Auth from "./pages/auth/Auth.svelte";
  import Dashboard from "./pages/dashboard/Dashboard.svelte";
  import { Router, Route } from "svelte-routing";

  export let url = "";
  export let version;
</script>

<Router {url}>
  <Route path="/" component={Dashboard} />
  <Route path="auth">
    <Auth {version} />
  </Route>
</Router>
