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

  let isOpen = false;

  function handleUpdate(event) {
    isOpen = event.detail.isOpen;
  }
</script>

<style>
  span {
    font-weight: 100;
    font-size: larger;
  }
</style>

<div use:links>
<Navbar color="primary" dark expand="md">
    <NavbarBrand href="/"><span>IAM</span></NavbarBrand>
    <NavbarToggler on:click={() => (isOpen = !isOpen)} />
    <Collapse {isOpen} navbar expand="md" on:update={handleUpdate}>
      <Nav class="ml-auto" navbar>
        <AuthGard>
          <NavItem>
            <NavLink href="admin">Administration</NavLink>
          </NavItem>
          <NavItem>
            <NavLink href="auth">Sign out</NavLink>
          </NavItem>
        </AuthGard>
        <AuthGard reverse>
          <NavItem>
            <NavLink href="auth">Sign in</NavLink>
          </NavItem>
        </AuthGard>
      </Nav>
    </Collapse>
  </Navbar>
</div>
