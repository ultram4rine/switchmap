<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Fab, { Label } from "@smui/fab";

  import FloorCard from "../components/cards/FloorCard.svelte";
  import FloorForm from "../components/forms/FloorForm.svelte";

  import type { FloorResponse } from "../interfaces/floor";

  export let shortName;

  let floorForm = false;

  let floors: Array<FloorResponse> = [
    {
      number: 1,
      switchesNumber: 2,
    },
    {
      number: 2,
      switchesNumber: 1,
    },
  ];
</script>

<LayoutGrid>
  {#each floors as f (f.number)}
    <Cell span={3}>
      <FloorCard floor={f} />
    </Cell>
  {/each}
</LayoutGrid>

<FloorForm bind:form={floorForm} />

<Fab
  color="primary"
  extended
  style="position:fixed; bottom: 16px; right: 16px;"
  on:click={() => (floorForm = true)}
>
  <Label>Add Floor</Label>
</Fab>
