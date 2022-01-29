<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
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
