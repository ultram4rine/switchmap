<template>
  <div id="layout" :v-bind="isLoading">
    <v-navigation-drawer v-model="drawer" app dark clipped floating>
      <v-list>
        <v-list-item-group>
          <v-list-item v-for="(nav, i) in navs" :key="i" :to="nav.link">
            <v-list-item-icon>
              <v-icon v-text="nav.icon"></v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title v-text="nav.text"></v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app dark clipped-left>
      <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
      <v-toolbar-title>
        SwitchMap
        <!-- <v-breadcrumbs :items="breadcrumbs" large></v-breadcrumbs> -->
      </v-toolbar-title>
      <v-spacer />
      <v-btn color="orange darken-1" @click="logout">
        Sign out
        <v-icon right>{{ mdiLogout }}</v-icon>
      </v-btn>
      <v-progress-linear
        :active="isLoading"
        :indeterminate="isLoading"
        absolute
        bottom
        color="orange accent-4"
      ></v-progress-linear>
    </v-app-bar>

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
import { useStore } from "vuex";

import {
  mdiLogout,
  mdiOfficeBuilding,
  mdiRouterNetwork,
  mdiLan,
} from "@mdi/js";

import { AUTH_LOGOUT } from "@/store/actions";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  setup() {
    const router = useRouter();
    const store = useStore();

    const drawer = ref(true);
    const navs = [
      { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
      { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
      { link: "/vis", text: "Visualization", icon: mdiLan },
    ];

    const logout = () => {
      store.dispatch(AUTH_LOGOUT).then(() => {
        router.push("/login");
      });
    };

    //breadcrumbs: [],

    return {
      drawer,
      navs,

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
