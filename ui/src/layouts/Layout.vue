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
      <v-toolbar-title>SwitchMap</v-toolbar-title>
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

    <v-content>
      <v-container fluid>
        <slot :is-loading="isLoading" />
      </v-container>
    </v-content>

    <v-speed-dial
      v-model="fab"
      absolute
      bottom
      right
      direction="top"
      transition="slide-y-reverse-transition"
      z-index="1"
    >
      <template v-slot:activator>
        <v-btn v-model="fab" fab color="orange accent-3" @click="overlay=!fab?true:false">
          <v-icon v-if="fab">{{ mdiClose }}</v-icon>
          <v-icon v-else>{{ mdiPlus }}</v-icon>
        </v-btn>
      </template>
      <v-btn fab small color="orange accent-1" @click="overlay=!fab?true:false">
        <v-icon>{{ mdiOfficeBuilding }}</v-icon>
      </v-btn>
    </v-speed-dial>
    <v-overlay :value="overlay" z-index="2" @click.native="overlay=false, fab=false"></v-overlay>

    <v-footer app dark inset>
      <v-icon>{{ mdiCopyright }}</v-icon>
      <span class="px-4">2020</span>
    </v-footer>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {
  mdiPlus,
  mdiClose,
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
      mdiPlus: mdiPlus,
      mdiClose: mdiClose,
      mdiOfficeBuilding: mdiOfficeBuilding,
      mdiLogout: mdiLogout,
      mdiCopyright: mdiCopyright,

      drawer: true,
      fab: false,
      overlay: false,

      navs: [
        { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
        { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
        { link: "/vis", text: "Visualization", icon: mdiLan }
      ]
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