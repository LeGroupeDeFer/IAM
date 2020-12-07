<script>

  import {
    Container,
    Row,
    Col
  } from 'sveltestrap';
  import Icon from 'svelte-awesome';
  import { lineChart } from 'svelte-awesome/icons';
  import { link } from 'svelte-routing';
  import formatRelative from 'date-fns/formatRelative';
  import canStore from 'iam/stores/can';
  import { empty, last, filter, scaleColor, colorScales } from 'iam/lib';

  // State
  
  let name        = '';
  let fillingRate = 0.0;
  let samples     = [];

  $: now = new Date();
  
  $: lastUpdate = last(samples)
    ? formatRelative(last(samples).time, now)
    : 'Never';

  $: recentSamples = filter(s => (now - s.time) / 1000 < 24 * 3600)(samples);

  $: color = `color: ${scaleColor(
    colorScales.green,
    colorScales.red,
    fillingRate / 100
  )}`;

  $: detailsLink = `/can/${name}`;

  canStore.subscribe(({ focus }) => {
    if (focus === null) {
      name = '';
      fillingRate = 0.0;
      samples = [];
    } else {
      name = focus.id;
      fillingRate = focus.currentFill;
      samples = focus.data;
    }
  })

</script>

<div class="can-summary d-flex flex-column justify-content-between">
  <Container fluid>
    <Row>
      <Col>
        <div class="text-muted font-weight-bold font-shadow">
          Last update:
        </div>
        <div class="pt-1">
          {lastUpdate}
        </div>
      </Col>
    </Row>
    <Row>
      <Col>
        <div class="pt-4 text-muted font-weight-bold font-shadow">
          Current filling rate:
        </div>
        <div class="pt-1">
          <b style={color}>{fillingRate}</b>
        </div>
      </Col>
    </Row>
    <Row>
      <Col>
        <div class="pt-4 text-muted font-weight-bold font-shadow">
          Samples / 24 hours:
        </div>
        <div class="pt-1 pb-2">{recentSamples.length}</div>
      </Col>
    </Row>
  </Container>

  <div class="can-see-more">
    <a
      href={detailsLink}
      use:link
      class="d-flex align-items-center justify-content-center btn btn-primary"
    >
      <Icon data={lineChart} class="mr-2" />
      Expand
    </a>
  </div>
</div>
