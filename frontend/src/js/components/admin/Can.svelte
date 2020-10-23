<script>
  import { Card, Container, Row, Col, Button } from "sveltestrap";
  import TextInput from "../overall/TextEdit.svelte";

  export let can;

  // TODO : Connect to api
</script>

<Card body>
  <Container>
    <Row>
      <Col xs="10"><TextInput text={can.id}/></Col>
      <Col xs="2">
        <Button outline color="danger" size="sm">Delete</Button>
      </Col>
    </Row>
  </Container>
</Card>
