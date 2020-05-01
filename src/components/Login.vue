<template>
  <v-content>
    <v-container class="fill-height" fluid>
      <v-row align="center" justify="center">
        <v-col cols="12" sm="8" md="4">
          <v-card class="elevation-12">
            <v-toolbar dark flat>
              <v-toolbar-title>Login to SwitchMap</v-toolbar-title>
            </v-toolbar>
            <v-card-text>
              <v-form>
                <v-text-field
                  v-model="username"
                  label="Name"
                  color="orange darken-2"
                  :prepend-icon="this.mdiAccount"
                  required
                ></v-text-field>
                <v-text-field
                  v-model="password"
                  label="Password"
                  color="orange darken-2"
                  :prepend-icon="this.mdiKey"
                  required
                ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <v-btn color="orange darken-1" class="mr-4" @click="login">
                Sign in
                <v-icon right>{{ mdiLogin }}</v-icon>
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-content>
</template>

<script lang="ts">
import Vue from "vue";
import axios from "axios";
import { mdiAccount, mdiKey, mdiLogin } from "@mdi/js";

import { AUTH_LOGIN } from "../store/actions";

export default Vue.extend({
  data() {
    return {
      username: "",
      password: "",

      mdiAccount: mdiAccount,
      mdiKey: mdiKey,
      mdiLogin: mdiLogin
    };
  },

  methods: {
    login: function() {
      const { username, password } = this;
      this.$store.dispatch(AUTH_LOGIN, { username, password }).then(() => {
        this.$router.push("/");
      });
    }
  }
});
</script>