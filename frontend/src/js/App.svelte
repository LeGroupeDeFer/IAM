<script>

  import { Router, Route } from 'svelte-routing';
  import Navbar from './components/overall/Navbar.svelte';
  import Admin from './pages/admin/Admin.svelte';
  import Dashboard from './pages/dashboard/Dashboard.svelte';
  import Can from './pages/can/Can.svelte';
  import Auth from './pages/auth/Auth.svelte';

  export let url = '';
  export let version;

</script>

<Router {url}>
  
  <Navbar />
  
  <Route path="/">
    <Dashboard />
  </Route>

  <Route path="admin">
    <Admin />
  </Route>
  
  <Route path="can/:id">
    <Can />
  </Route>

  <Route path="auth">
    <Auth {version} />
  </Route>

</Router>
