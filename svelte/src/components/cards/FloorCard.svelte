<script lang="ts">
  import Card, { Actions, ActionButtons } from "@smui/card";
  import Button, { Label } from "@smui/button";

  import type { FloorResponse } from "../../interfaces/floor";

  export let floor: FloorResponse;
</script>

<Card variant="outlined">
  <div style="padding: 1rem;">
    <h2 class="mdc-typography--headline6" style="margin: 0;">
      Floor {floor.number}
    </h2>
    <h3 class="mdc-typography--subtitle2" style="margin: 0; color: #888;">
      {floor.switchesNumber}
      {floor.switchesNumber !== 1 ? "switches" : "switch"}
    </h3>
  </div>
  <Actions>
    <ActionButtons>
      <Button>
        <Label>Add switch</Label>
      </Button>
      <Button>
        <Label>Go</Label>
      </Button>
    </ActionButtons>
  </Actions>
</Card>
