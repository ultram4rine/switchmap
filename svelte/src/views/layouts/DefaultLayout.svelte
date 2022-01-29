<script lang="ts">
  import TopAppBar, { Row, Section, Title } from "@smui/top-app-bar";
  import Drawer, { Content, AppContent } from "@smui/drawer";
  import List, { Item, Text, Graphic } from "@smui/list";
  import IconButton from "@smui/icon-button";
  import Button, { Label, Icon } from "@smui/button";
  import LinearProgress from "@smui/linear-progress";

  import { Route } from "svelte-router-spa";

  export let currentRoute;
  export const params = {};

  const navs = [
    { link: "/builds", text: "Builds", icon: "apartment" },
    { link: "/switches", text: "Switches", icon: "router" },
    { link: "/vis", text: "Visualization", icon: "account_tree" },
  ];

  let drawerOpen = false;
</script>

<TopAppBar variant="fixed" style="z-index: 7;">
  <Row>
    <Section>
      <IconButton
        class="material-icons"
        on:click={() => (drawerOpen = !drawerOpen)}
      >
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
<LinearProgress
  indeterminate
  style="padding-top: 64px; z-index: 6;"
  slot="progress"
/>

<Drawer
  variant="dismissible"
  bind:open={drawerOpen}
  style="height: calc(100% - 64px - 4px);"
>
  <Content>
    <List>
      {#each navs as nav (nav.link)}
        <Item
          href={nav.link}
          activated={currentRoute.name.startsWith(nav.link)}
        >
          <Graphic class="material-icons" aria-hidden="true">{nav.icon}</Graphic
          >
          <Text>{nav.text}</Text>
        </Item>
      {/each}
    </List>
  </Content>
</Drawer>

<AppContent class="app-content">
  <main class="main-content">
    <Route {currentRoute} {params} />
  </main>
</AppContent>

<style>
  * :global(.app-content) {
    flex: auto;
    overflow: auto;
    position: relative;
    flex-grow: 1;
  }

  .main-content {
    overflow: auto;
    /* padding: 80px 16px 16px; */
    height: 100%;
    box-sizing: border-box;
  }
</style>
