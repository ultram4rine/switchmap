<template>
  <v-layout :v-bind="isLoading">
    <v-app-bar app>
      <template v-slot:prepend>
        <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
      </template>
      <v-app-bar-title>SwitchMap</v-app-bar-title>
      <template v-slot:append>
        <v-btn
          variant="elevated"
          color="orange darken-1"
          :append-icon="mdiLogout"
          @click="logout"
        >
          Sign out
        </v-btn>
      </template>
      <!-- <v-progress-linear
        :active="isLoading"
        :indeterminate="isLoading"
        absolute
        bottom
        color="orange accent-4"
      ></v-progress-linear> -->
    </v-app-bar>

    <v-navigation-drawer v-model="drawer" app>
      <v-list>
        <v-list-group>
          <v-list-item v-for="(nav, i) in navs" :key="i" :to="nav.link">
            <template v-slot:prepend>
              <v-icon :icon="nav.icon"></v-icon>
            </template>
            <v-list-item-title v-text="nav.text"></v-list-item-title>
          </v-list-item>
        </v-list-group>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container fluid>
        <router-view :is-loading="isLoading" />
      </v-container>
    </v-main>
  </v-layout>
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

    return {
      drawer,
      navs,

      authStore,

      mdiLogout,

      logout,
    };
  },
});
</script>
