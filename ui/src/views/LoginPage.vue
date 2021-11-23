<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card class="elevation-12">
          <v-toolbar dark flat>
            <v-toolbar-title>Login to SwitchMap</v-toolbar-title>
          </v-toolbar>
          <v-form @submit.prevent="login">
            <v-card-text>
              <v-text-field
                v-model="username"
                label="Name"
                type="text"
                color="orange darken-1"
                required
                :prepend-icon="mdiAccount"
              ></v-text-field>
              <v-text-field
                v-model="password"
                label="Password"
                :type="show ? 'text' : 'password'"
                color="orange darken-1"
                required
                :prepend-icon="mdiKey"
                :append-icon="show ? mdiEye : mdiEyeOff"
                @click:append="show = !show"
              ></v-text-field>
              <v-checkbox
                v-model="rememberMe"
                label="Remember me"
                color="orange darken-1"
              ></v-checkbox>
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <v-btn type="submit" color="orange darken-1" class="mr-4">
                Sign in
                <v-icon right>{{ mdiLogin }}</v-icon>
              </v-btn>
            </v-card-actions>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { mdiEye, mdiEyeOff, mdiAccount, mdiKey, mdiLogin } from "@mdi/js";

import { AUTH_LOGIN } from "@/store/actions";

export default defineComponent({
  setup() {
    const router = useRouter();
    const store = useStore();

    const username = ref("");
    const password = ref("");
    const rememberMe = ref(false);

    const show = ref(false);

    const login = async () => {
      await store.dispatch(AUTH_LOGIN, { username, password, rememberMe });
      router.push("/");
    };

    return {
      username,
      password,
      rememberMe,

      show,

      login,

      mdiEye,
      mdiEyeOff,
      mdiAccount,
      mdiKey,
      mdiLogin,
    };
  },
});
</script>
