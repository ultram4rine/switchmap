<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Textfield from "@smui/textfield";

  import FormWrap from "../FormWrap.svelte";

  export let form = false;

  let number = 0;
</script>

<FormWrap bind:form title="Add floor" action="add">
  <LayoutGrid>
    <Cell span={12}>
      <Textfield
        bind:value={number}
        label="Number"
        type="number"
        style="width: 100%;"
        helperLine$style="width: 100%;"
      />
    </Cell>
  </LayoutGrid>
</FormWrap>
