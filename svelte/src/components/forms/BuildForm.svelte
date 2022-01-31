<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Textfield from "@smui/textfield";

  import FormWrap from "../FormWrap.svelte";

  export let form = false;

  let name = "";
  let shortName = "";
</script>

<FormWrap {form} title="Add build" action="add">
  <LayoutGrid>
    <Cell span={12}>
      <Textfield
        bind:value={name}
        label="Name"
        type="text"
        style="width: 100%;"
        helperLine$style="width: 100%;"
      />
    </Cell>
    <Cell span={12}>
      <Textfield
        bind:value={shortName}
        label="Short name"
        type="text"
        style="width: 100%;"
        helperLine$style="width: 100%;"
      />
    </Cell>
  </LayoutGrid>
</FormWrap>
