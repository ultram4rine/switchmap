<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" app dark clipped floating>
      <v-list>
        <v-list-item-group v-model="nav">
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
        <router-view></router-view>
      </v-container>
    </v-content>

    <v-footer app dark inset>
      <span class="px-4">&copy; {{ 2020 }}</span>
    </v-footer>
  </v-app>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      isLoading: false,
      drawer: null,
      nav: 0,
      navs: [
        { link: "/", text: "Builds", icon: "mdi-office-building" },
        { link: "/vis", text: "Visualization", icon: "mdi-lan" }
      ]
    };
  },

  methods: {
    setLoading(isLoading) {
      if (isLoading) {
        this.isLoading = true;
      } else {
        this.isLoading = false;
      }
    }
  },

  created() {
    axios.interceptors.request.use(
      config => {
        this.setLoading(true);
        return config;
      },
      error => {
        this.setLoading(false);
        return Promise.reject(error);
      }
    );

    axios.interceptors.response.use(
      response => {
        this.setLoading(false);
        return response;
      },
      error => {
        this.setLoading(false);
        return Promise.reject(error);
      }
    );
  }
};
</script>
