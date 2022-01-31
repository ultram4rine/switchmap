<script lang="ts">
  import Dialog, { Title, Content, Actions } from "@smui/dialog";
  import Button, { Label } from "@smui/button";

  export let form = false;
  export let title = "";
  export let action = "";
</script>

<Dialog bind:open={form} surface$style="width: 500px; max-width: 500px;">
  <Title>{title}</Title>
  <Content>
    <slot />
  </Content>
  <Actions>
    <Button>
      <Label>Cancel</Label>
    </Button>
    <Button action="accept" on:click={() => (form = false)}>
      <Label>{action}</Label>
    </Button>
  </Actions>
</Dialog>
