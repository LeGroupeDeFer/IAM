<script>
  import AuthGuard from "../../components/auth/AuthGuard.svelte";
  import Panel from "../../components/admin/Panel.svelte";
  import { Link } from "svelte-routing";
</script>

<style>
  .container {
    min-height: 75vh;
    padding-top: 5vh;
    vertical-align: middle;
  }
  .center {
    text-align: center;
  }
</style>

<div class="container">
  <h1 class="text-primary">Administration panel</h1>
  <hr />
  <AuthGuard reverse>
    <p class="center">
      Please,
      <Link to="auth">authenticate</Link>
      yourself first.
    </p>
  </AuthGuard>
  <AuthGuard>
    <Panel />
  </AuthGuard>
</div>
