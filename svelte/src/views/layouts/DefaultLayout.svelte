<script lang="ts">
  import TopAppBar, { Row, Section, Title } from "@smui/top-app-bar";
  import Drawer, { Content, AppContent, Scrim } from "@smui/drawer";
  import List, { Item, Text, Graphic, Separator, Subheader } from "@smui/list";
  import IconButton from "@smui/icon-button";
  import Button, { Label, Icon } from "@smui/button";

  let open = false;
</script>

<Drawer variant="dismissible" bind:open>
  <Content>
    <List>
      <Item>
        <Graphic class="material-icons" aria-hidden="true">inbox</Graphic>
        <Text>Inbox</Text>
      </Item>
      <Item>
        <Graphic class="material-icons" aria-hidden="true">star</Graphic>
        <Text>Star</Text>
      </Item>
      <Item>
        <Graphic class="material-icons" aria-hidden="true">send</Graphic>
        <Text>Sent Mail</Text>
      </Item>
    </List>
  </Content>
</Drawer>

<Scrim />
<TopAppBar variant="fixed" class="appbar">
  <Row>
    <Section>
      <IconButton class="material-icons" on:click={() => (open = !open)}>
        menu
      </IconButton>
      <Title>SwitchMap</Title>
    </Section>
    <Section align="end" toolbar>
      <Button variant="raised" color="primary">
        <Label>Sign out</Label>
        <Icon class="material-icons">logout</Icon>
      </Button>
    </Section>
  </Row>
</TopAppBar>
<AppContent class="app-content">
  <main class="main-content">
    <slot />
  </main>
</AppContent>

<style>
  .appbar {
    background-color: #272727;
  }
  * :global(.app-content) {
    flex: auto;
    overflow: auto;
    position: relative;
    flex-grow: 1;
  }

  .main-content {
    overflow: auto;
    padding: 16px;
    height: 100%;
    box-sizing: border-box;
  }
</style>
