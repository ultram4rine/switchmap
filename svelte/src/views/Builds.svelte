<script lang="ts">
  import LayoutGrid, { Cell } from "@smui/layout-grid";
  import BuildCard from "../components/cards/BuildCard.svelte";

  import type { BuildResponse } from "../interfaces/build";

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
