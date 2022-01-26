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

import { useAuth } from "@/store/auth";

import api from "@/api";

const defaultLayout = "default-layout";

export default defineComponent({
  setup() {
    const router = useRouter();
    const route = useRoute();

    const authStore = useAuth();

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
          authStore.logout();
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
