<script>
  
  import { navigate } from 'svelte-routing';
  import {
    Container,
    Row,
    Col
  } from 'sveltestrap';
  import Chart from 'svelte-frappe-charts';

  import canStore from 'iam/stores/can';
  import { lastDayActivity, lastWeekActivity } from 'iam/lib/graph';

  if (!$canStore.focus)
    navigate('/');
  
  $: pastDay = lastDayActivity($canStore.focus);
  $: pastWeek = lastWeekActivity($canStore.focus);

</script>

<div class="can-detail fade-in">
  <Container fluid>
    <Row>
      <Col>
        <h1 class="text-muted">{$canStore.focus.id}</h1>
        <hr />
      </Col>
    </Row>
    <Row>
      <Col>
        <h2 class="text-muted font-weight-bold">Filling rate: last 24 hours</h2>
        <Chart data={pastDay} type="line" />
      </Col>
    </Row>
    <Row>
      <Col>
        <h2 class="text-muted font-weight-bold">Filling rate: last 7 days</h2>
        <Chart data={pastWeek} type="line" />
      </Col>
    </Row>
  </Container>
</div>
