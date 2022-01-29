<script lang="ts">
  import Card, { Actions, ActionButtons } from "@smui/card";
  import Button, { Label } from "@smui/button";

  import { navigateTo } from "svelte-router-spa";

  import type { BuildResponse } from "../../interfaces/build";

  export let build: BuildResponse;
</script>

<Card variant="outlined">
  <div style="padding: 1rem;">
    <h2 class="mdc-typography--headline6" style="margin: 0;">
      {build.name}
    </h2>
    <h3 class="mdc-typography--subtitle2" style="margin: 0; color: #888;">
      {build.floorsNumber}
      {build.floorsNumber !== 1 ? "floors" : "floor"},
      {build.switchesNumber}
      {build.switchesNumber !== 1 ? "switches" : "switch"}
    </h3>
  </div>
  <Actions>
    <ActionButtons>
      <Button>
        <Label>Add floor</Label>
      </Button>
      <Button on:click={() => navigateTo(`builds/${build.shortName}`)}>
        <Label>Go</Label>
      </Button>
    </ActionButtons>
  </Actions>
</Card>
