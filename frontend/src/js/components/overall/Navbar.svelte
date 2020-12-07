<script>
  import { links } from "svelte-routing";
  import {
    Collapse,
    Navbar,
    NavbarToggler,
    NavbarBrand,
    Nav,
    NavItem,
    NavLink,
  } from "sveltestrap";
  import AuthGard from "../auth/AuthGuard.svelte";
  import Icon from "svelte-awesome";
  import { userCircle } from "svelte-awesome/icons";


  let isOpen = false;

  function handleUpdate(event) {
    isOpen = event.detail.isOpen;
  }
</script>

<style>
  h1 {
    font-weight: bold;
    font-size: 2.5rem;
    margin-bottom: 0;
  }
</style>

<div class="app-nav" use:links>
  <Navbar light expand="md">
    
    <NavbarBrand href="/">
      <h1 class="font-shadow">IAM</h1>
    </NavbarBrand>
    
    <NavbarToggler on:click={() => (isOpen = !isOpen)} />
    
    <Collapse {isOpen} navbar expand="md" on:update={handleUpdate}>
      <Nav class="ml-auto" navbar>
        <AuthGard>
          <NavItem>
            <NavLink href="/admin">
              Administration
            </NavLink>
          </NavItem>
          <NavItem>
            <NavLink href="/auth">
              Sign out
            </NavLink>
          </NavItem>
        </AuthGard>
        <AuthGard reverse>
          <NavItem>
            <NavLink href="/auth">
              <b>Sign in</b>
            </NavLink>
          </NavItem>
        </AuthGard>
      </Nav>
    </Collapse>

  </Navbar>
</div>
