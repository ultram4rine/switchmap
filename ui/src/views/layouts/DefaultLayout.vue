<template>
  <v-layout :v-bind="isLoading">
    <v-app-bar app>
      <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
      <v-app-bar-title>SwitchMap</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-btn
        color="primary"
        :icon="mdiThemeLightDark"
        @click="toggleTheme"
      ></v-btn>
      <v-divider vertical inset class="ma-2"></v-divider>
      <v-btn color="orange darken-1" :append-icon="mdiLogout" @click="logout">
        Sign out
      </v-btn>
      <!-- <v-progress-linear
        :active="isLoading"
        :indeterminate="isLoading"
        absolute
        bottom
        color="orange accent-4"
      ></v-progress-linear> -->
    </v-app-bar>

    <v-navigation-drawer v-model="drawer" app>
      <v-list density="compact" nav>
        <v-list-item
          v-for="(nav, i) in navs"
          :key="i"
          :to="nav.link"
          :prepend-icon="nav.icon"
        >
          <v-list-item-title>{{ nav.text }}</v-list-item-title>
        </v-list-item>
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
import { useTheme } from "vuetify";

import { useAuth } from "@/store/auth";

import {
  mdiThemeLightDark,
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
    const theme = useTheme();

    const drawer = ref(true);
    const navs = [
      { link: "/builds", text: "Builds", icon: mdiOfficeBuilding },
      { link: "/switches", text: "Switches", icon: mdiRouterNetwork },
      { link: "/vis", text: "Visualization", icon: mdiLan },
    ];

    const toggleTheme = () =>
      (theme.global.name.value = theme.global.current.value.dark
        ? "light"
        : "dark");
    const logout = async () => {
      await authStore.logout();
      router.push("/login");
    };

    return {
      drawer,
      navs,

      authStore,

      mdiThemeLightDark,
      mdiLogout,

      toggleTheme,
      logout,
    };
  },
});
</script>
