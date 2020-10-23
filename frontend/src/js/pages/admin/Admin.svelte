<script>
  import Navbar from "../../components/overall/Navbar.svelte";
  import AuthGuard from "../../components/auth/AuthGuard.svelte";
  import { Link } from "svelte-routing";
</script>

<Navbar />
<main>
  <AuthGuard><b>Administration TODO</b></AuthGuard>

  <AuthGuard reverse>
    <p>Please, <Link to="auth">authenticate</Link> yourself first.</p>
  </AuthGuard>
</main>
