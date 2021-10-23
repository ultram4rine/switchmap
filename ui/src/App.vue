<template>
  <v-app>
    <component :is="layout" :is-loading="isLoading">
      <template v-slot="props">
        <router-view :is-loading="props.isLoading"></router-view>
      </template>
    </component>
  </v-app>
</template>

<script lang="ts">
import Vue from "vue";

import api from "./api";
import { AUTH_LOGOUT } from "./store/actions";

const defaultLayout = "default";

export default Vue.extend({
  data() {
    return {
      isLoading: false,
    };
  },

  methods: {
    setLoading(isLoading: boolean) {
      if (isLoading) {
        this.isLoading = true;
      } else {
        this.isLoading = false;
      }
    },
  },

  computed: {
    layout() {
      return this.$route.meta ? this.$route.meta.layout : defaultLayout;
    },
  },

  created() {
    api.interceptors.request.use(
      (config) => {
        this.setLoading(true);
        return config;
      },
      (error) => {
        this.setLoading(false);
        return Promise.reject(error);
      }
    );

    api.interceptors.response.use(
      (response) => {
        this.setLoading(false);
        return response;
      },
      (error) => {
        this.setLoading(false);
        return new Promise(() => {
          if (error.response.status && error.response.status === 401) {
            this.$store.dispatch(AUTH_LOGOUT);
            this.$router.push("/login");
          } else {
            return Promise.reject(error);
          }
        });
      }
    );
  },
});
</script>
