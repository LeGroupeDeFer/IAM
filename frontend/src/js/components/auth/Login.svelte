<script>
  import { Form, FormGroup, Input, Label, Button } from 'sveltestrap';
  import auth from 'iam/stores/auth';
  import AuthGuard from './AuthGuard.svelte';

  let username = '';
  let password = '';
  $: canLogin = username.length && password.length;
  const clear = () => username = password = '';
  const onLogin = e => e.preventDefault() || auth.login(username, password).then(clear);
  const onLogout = e => e.preventDefault() || auth.logout();
</script>


<Form>
  <AuthGuard>
    <p>Hello, {$auth.sub}!</p>
    <hr />
  </AuthGuard>

  <FormGroup>
    <Label for="username">Username</Label>
    <Input
      type="text"
      bind:value={username}
    />
  </FormGroup>
  
  <FormGroup>
    <Label for="password">Password</Label>
    <Input
      type="password"
      bind:value={password}
    />
  </FormGroup>

  <hr />

  <AuthGuard reverse>
    <Button
      disabled={!canLogin}
      on:click={onLogin}
      color="primary"
      class="d-block mx-auto"
    >
      Login
    </Button>
  </AuthGuard>
  <AuthGuard>
    <Button
      on:click={onLogout}
      color="secondary"
      class="d-block mx-auto"
    >
      Logout
    </Button>
  </AuthGuard>
</Form>