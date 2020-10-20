<script>
  
  import { Form, FormGroup, Input, Label, Button } from 'sveltestrap';
  import auth from 'iam/stores/auth';
  import AuthGuard from './AuthGuard.svelte';

  let username = '';
  let password = '';
  $: canLogin = username.length && password.length;
  
  const clear = () => username = password = '';
  const onLogin = e => e.preventDefault() || auth.login(username, password) && clear();
  const onLogout = e => e.preventDefault() || auth.logout();

</script>


<Form>

  <AuthGuard>
    <h2>Hello, {$auth.sub}!</h2>
    <hr />
    <Button
      on:click={onLogout}
      color="danger"
      class="d-block mx-auto"
    >
      Logout
    </Button>
  </AuthGuard>

  <AuthGuard reverse>
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

    <Button
      disabled={!canLogin}
      on:click={onLogin}
      color="primary"
      class="d-block mx-auto"
    >
      Login
    </Button>
  </AuthGuard>

</Form>