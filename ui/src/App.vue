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
import { mapActions, mapGetters } from "vuex";
import axios from "axios";

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

    ...mapActions("csrf", ["setToken"]),
    ...mapGetters("csrf", ["getToken"]),
  },

  computed: {
    layout() {
      return this.$route.meta.layout || defaultLayout;
    },
  },

  created() {
    const bodyHtmlTag = document.getElementsByTagName("body")[0];
    const csrfToken = bodyHtmlTag.getAttribute("csrf-token-value");
    this.setToken(csrfToken);

    axios.interceptors.request.use(
      config => {
        this.setLoading(true);
        const csrfToken = this.getToken();
        axios.defaults.headers.common["Csrf-Token"] = csrfToken;
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
        return new Promise(() => {
          if (
            err.status === 401 &&
            err.config &&
            !err.config.__isRetryRequest
          ) {
            this.$store.dispatch(AUTH_LOGOUT);
            this.$router.push("/login");
          }
          throw err;
        });
      }
    );
  },
});
</script>
