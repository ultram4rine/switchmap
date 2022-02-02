<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Fab, { Label } from "@smui/fab";

  import BuildCard from "../components/cards/BuildCard.svelte";
  import BuildForm from "../components/forms/BuildForm.svelte";

  import type { BuildResponse } from "../interfaces/build";

  let buildForm = false;

  let builds: Array<BuildResponse> = [
    {
      name: "Build 1",
      shortName: "b1",
      floorsNumber: 1,
      switchesNumber: 2,
    },
    {
      name: "Build 2",
      shortName: "b2",
      floorsNumber: 2,
      switchesNumber: 1,
    },
  ];
</script>

<LayoutGrid>
  {#each builds as b (b.shortName)}
    <Cell span={3}>
      <BuildCard build={b} />
    </Cell>
  {/each}
</LayoutGrid>

<BuildForm bind:form={buildForm} />

<Fab
  color="primary"
  extended
  style="position:fixed; bottom: 16px; right: 16px;"
  on:click={() => (buildForm = true)}
>
  <Label>Add build</Label>
</Fab>
