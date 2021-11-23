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
import { defineComponent, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";

import api from "@/api";
import { AUTH_LOGOUT } from "@/store/actions";

const defaultLayout = "default-layout";

export default defineComponent({
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();

    const isLoading = ref(false);

    const setLoading = (val: boolean) => {
      isLoading.value = val;
    };

    api.interceptors.request.use(
      (config) => {
        setLoading(true);
        return config;
      },
      (error) => {
        setLoading(false);
        return Promise.reject(error);
      }
    );

    api.interceptors.response.use(
      (response) => {
        setLoading(false);
        return response;
      },
      (error) => {
        setLoading(false);
        if (error.response.status && error.response.status === 401) {
          store.dispatch(AUTH_LOGOUT);
          router.push("/login");
        }
        return Promise.reject(error);
      }
    );

    return {
      isLoading,
      layout: computed(() => (route.meta ? route.meta.layout : defaultLayout)),
    };
  },
});
</script>
