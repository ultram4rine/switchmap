<template>
  <v-app>
    <component :is="layout">
      <router-view></router-view>
    </component>
  </v-app>
</template>

<script lang="ts">
import Vue from "vue";
import axios from "axios";

import { AUTH_LOGOUT } from "./store/actions";

const defaultLayout = "default";

export default Vue.extend({
  data() {
    return {
      isLoading: false
    };
  },

  methods: {
    setLoading(isLoading: boolean) {
      if (isLoading) {
        this.isLoading = true;
      } else {
        this.isLoading = false;
      }
    }
  },

  computed: {
    layout() {
      return this.$route.meta.layout || defaultLayout;
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
      err => {
        this.setLoading(false);
        return new Promise((resolve, reject) => {
          if (
            err.status === 401 &&
            err.config &&
            !err.config.__isRetryRequest
          ) {
            // if you ever get an unauthorized, logout the user
            this.$store.dispatch(AUTH_LOGOUT);
            this.$router.push("/login");
            // you can also redirect to /login if needed !
          }
          throw err;
        });
      }
    );
  }
});
</script>
