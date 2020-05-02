<template>
  <div :v-bind="isLoading">
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
      <v-toolbar-title>SwitchMap</v-toolbar-title>
      <v-spacer />
      <v-btn color="orange darken-1" class="mr-4" @click="logout">
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

    <v-content>
      <v-container fluid>
        <slot />
      </v-container>
    </v-content>

    <v-footer app dark inset>
      <v-icon>{{ mdiCopyright }}</v-icon>
      <span class="px-4">2020</span>
    </v-footer>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {
  mdiLogout,
  mdiOfficeBuilding,
  mdiRouterNetwork,
  mdiLan,
  mdiCopyright
} from "@mdi/js";

import { AUTH_LOGOUT } from "../store/actions";

interface Nav {
  text: string;
  link: string;
  icon: string;
}

export default Vue.extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  data() {
    return {
      drawer: true,

      navs: [
        { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
        { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
        { link: "/vis", text: "Visualization", icon: mdiLan }
      ],

      mdiLogout: mdiLogout,
      mdiCopyright: mdiCopyright
    };
  },

  methods: {
    logout: function() {
      this.$store.dispatch(AUTH_LOGOUT).then(() => {
        this.$router.push("/login");
      });
    }
  }
});
</script>