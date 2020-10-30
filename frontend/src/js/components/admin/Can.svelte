<script>
  import { Card, Container, Row, Col, Button } from "sveltestrap";
  import { createEventDispatcher } from "svelte";
  import TextInput from "../overall/TextEdit.svelte";

  export let can;

  const dispatch = createEventDispatcher();

  // TODO : Connect to api
</script>

<Card body>
  <Container>
    <Row>
      <Col xs="10">
        <TextInput text={can.id} />
      </Col>
      <Col xs="2">
        <Button
          outline
          color="danger"
          size="sm"
          on:click={() => dispatch('deleteToggled', { id: can.id })}>
          Delete
        </Button>
      </Col>
    </Row>
  </Container>
</Card>
