<script>
  import { navigate } from "svelte-routing";
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

  function goto(page) {
    navigate(`/${page}`);
  }

  function handleUpdate(event) {
    isOpen = event.detail.isOpen;
  }

  // FIXME : navigate() appends a # to the url - why ?
</script>

<style>
  span {
    font-weight: 100;
    font-size: larger;
  }
</style>

<Navbar color="primary" dark expand="md">
  <NavbarBrand href="/"><span>IAM</span></NavbarBrand>
  <NavbarToggler on:click={() => (isOpen = !isOpen)} />
  <Collapse {isOpen} navbar expand="md" on:update={handleUpdate}>
    <Nav class="ml-auto" navbar>
      <AuthGard>
        <NavItem>
          <NavLink on:click={() => goto('admin')}>Administration</NavLink>
        </NavItem>
        <NavItem>
          <NavLink on:click={() => goto('auth')}>Sign out</NavLink>
        </NavItem>
      </AuthGard>

      <AuthGard reverse>
        <NavItem>
          <NavLink on:click={() => goto('auth')}>Sign in</NavLink>
        </NavItem>
      </AuthGard>
    </Nav>
  </Collapse>
</Navbar>
