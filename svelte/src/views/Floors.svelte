<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import Fab, { Label } from "@smui/fab";

  import FloorCard from "../components/cards/FloorCard.svelte";

  import type { FloorResponse } from "../interfaces/floor";

  export let shortName;

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

<Fab
  color="primary"
  extended
  style="position:fixed; bottom: 16px; right: 16px;"
>
  <Label>Add Floor</Label>
</Fab>
