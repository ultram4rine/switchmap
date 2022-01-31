<script lang="ts">
  import TopAppBar, { Row, Section, Title } from "@smui/top-app-bar";
  import Drawer, { Content, AppContent } from "@smui/drawer";
  import List, { Item, Text, Graphic } from "@smui/list";
  import IconButton from "@smui/icon-button";
  import Button, { Label } from "@smui/button";
  import LinearProgress from "@smui/linear-progress";

  import SVGIcon from "../../components/SVGIcon.svelte";

  import {
    mdiMenu,
    mdiLogout,
    mdiOfficeBuilding,
    mdiRouterNetwork,
    mdiLan,
  } from "@mdi/js";

  import { Route } from "svelte-router-spa";

  import api from "../../api";

  export let currentRoute;
  export const params = {};

  const navs = [
    { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
    { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
    { link: "/vis", text: "Visualization", icon: mdiLan },
  ];

  let drawerOpen = true;
  let isLoading = false;

  api.interceptors.request.use(
    (config) => {
      isLoading = true;
      return config;
    },
    (error) => {
      isLoading = false;
      return Promise.reject(error);
    }
  );

  api.interceptors.response.use(
    (response) => {
      isLoading = false;
      return response;
    },
    (error) => {
      isLoading = false;
      /* if (error.response.status && error.response.status === 401) {
          this.$store.dispatch(AUTH_LOGOUT);
          this.$router.push("/login");
        } */
      return Promise.reject(error);
    }
  );
</script>

<TopAppBar
  variant="fixed"
  style="background-color: #212125; z-index: 7; box-shadow: 0 2px 4px -1px rgb(0 0 0 / 20%), 0 4px 5px 0 rgb(0 0 0 / 14%), 0 1px 10px 0 rgb(0 0 0 / 12%);"
>
  <Row>
    <Section>
      <IconButton on:click={() => (drawerOpen = !drawerOpen)}>
        <SVGIcon icon={mdiMenu} />
      </IconButton>
      <Title>SwitchMap</Title>
    </Section>
    <Section align="end" toolbar>
      <Button variant="raised" color="primary">
        <Label>Sign out</Label>
        <SVGIcon icon={mdiLogout} />
      </Button>
    </Section>
  </Row>
  <LinearProgress indeterminate closed={!isLoading} style="z-index: 8;" />
</TopAppBar>

<Drawer variant="dismissible" bind:open={drawerOpen} style="padding-top: 68px;">
  <Content>
    <List>
      {#each navs as nav (nav.link)}
        <Item
          href={nav.link}
          activated={currentRoute.name.startsWith(nav.link)}
        >
          <Graphic aria-hidden="true"><SVGIcon icon={nav.icon} /></Graphic>
          <Text>{nav.text}</Text>
        </Item>
      {/each}
    </List>
  </Content>
</Drawer>

<AppContent class="app-content">
  <main class="main-content" style="padding-top: 68px;">
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
