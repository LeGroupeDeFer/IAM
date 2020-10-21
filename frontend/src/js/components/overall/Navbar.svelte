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

  let isOpen = false;

  function goto(page) {
    navigate(`/${page}`);
  }

  function handleUpdate(event) {
    isOpen = event.detail.isOpen;
  }

  // TODO : Add admin link if user is connected
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
      <NavItem>
        <NavLink on:click={() => goto('auth')}>Sign in</NavLink>
      </NavItem>
    </Nav>
  </Collapse>
</Navbar>
