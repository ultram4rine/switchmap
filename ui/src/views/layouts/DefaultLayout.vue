<template>
  <div id="layout" :v-bind="isLoading">
    <v-app-bar app dark absolute clipped-left>
      <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
      <v-app-bar-title>
        SwitchMap
        <!-- <v-breadcrumbs :items="breadcrumbs" large></v-breadcrumbs> -->
      </v-app-bar-title>
      <v-progress-linear
        :active="isLoading"
        :indeterminate="isLoading"
        absolute
        bottom
        color="orange accent-4"
      ></v-progress-linear>
      <v-spacer></v-spacer>
      <v-btn color="orange darken-1" @click="logout">
        Sign out
        <v-icon right :icon="mdiLogout"></v-icon>
      </v-btn>
    </v-app-bar>
    <v-navigation-drawer v-model="drawer" app dark absolute temporary>
      <v-list nav dense>
        <v-list-item
          v-for="(nav, i) in navs"
          :key="i"
          :value="nav"
          :to="nav.link"
        >
          <v-list-item-avatar left>
            <v-icon :icon="nav.icon"></v-icon>
          </v-list-item-avatar>
          <v-list-item-title>{{ nav.text }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container fluid>
        <slot :is-loading="isLoading" />
      </v-container>
    </v-main>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { useRouter } from "vue-router";

import { useAuth } from "@/store/auth";

import {
  mdiLogout,
  mdiOfficeBuilding,
  mdiRouterNetwork,
  mdiLan,
} from "@mdi/js";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  setup() {
    const router = useRouter();
    const authStore = useAuth();

    const drawer = ref(true);
    const navs = [
      { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
      { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
      { link: "/vis", text: "Visualization", icon: mdiLan },
    ];

    const logout = async () => {
      await authStore.logout();
      router.push("/login");
    };

    //breadcrumbs: [],

    return {
      drawer,
      navs,

      authStore,

      mdiLogout,

      logout,
    };
  },

  /* created() {
    console.log(this.$route.path.split("/"));
    this.breadcrumbs = [
      {
        text: "SwitchMap",
        disabled: true,
      },
    ];
    const arr = this.$route.path.split("/");
    let href = `/${arr[1]}`;
    if (arr.length > 2) {
      for (let i = 2; i < arr.length; i++) {
        href = `${href}/${arr[i]}`;
        this.breadcrumbs.push({
          text: arr[i],
          disabled: href === this.$route.path,
          href: href,
        });
      }
    }
  }, */
});
</script>

<style>
/* .v-application ol,
.v-application ul {
  padding-left: 0;
}
.theme--dark.v-breadcrumbs .v-breadcrumbs__divider,
.theme--dark.v-breadcrumbs .v-breadcrumbs__item--disabled {
  color: white;
}
.v-breadcrumbs--large li,
.v-breadcrumbs--large li .v-icon {
  font-size: 1.25rem;
} */
</style>
