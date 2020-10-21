<script>
  import Form from "../../components/auth/Form.svelte";
  import { Link } from "svelte-routing";

  export let version;
</script>

<style>
  .content {
    width: 300px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  h1 {
    font-size: 5em;
  }

  .git-link {
    color: #62929e;
  }

  .git-link:hover {
    text-decoration: none;
    border-bottom: 3px solid #393d3f;
    padding-bottom: 3px;
  }
</style>

<div class="content text-center">
  <p class="text-muted my-0">v{version}</p>
  <Link>
    <h1 class="text-primary py-0 my-0">IAM</h1>
  </Link>
  <a href="https://github.com/LeGroupeDeFer/IAM" class="git-link">
    View on GitHub
  </a>

  <hr />
  <Form />
</div>
