<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12">
        <v-card class="elevation-12">
          <v-toolbar dark flat>
            <v-toolbar-title>Login to SwitchMap</v-toolbar-title>
          </v-toolbar>

          <v-card-text>
            <ValidationObserver ref="observer" v-slot="{ validate }">
              <v-form>
                <ValidationProvider v-slot="{ errors }" name="Username" rules="required">
                  <v-text-field
                    v-model="username"
                    label="Username"
                    :error-messages="errors"
                    color="orange darken-1"
                    required
                    :prepend-icon="this.mdiAccount"
                  ></v-text-field>
                </ValidationProvider>
                <ValidationProvider v-slot="{ errors }" name="Password" rules="required">
                  <v-text-field
                    v-model="password"
                    label="Password"
                    :type="show ? 'text' : 'password'"
                    :error-messages="errors"
                    color="orange darken-1"
                    required
                    :prepend-icon="this.mdiKey"
                    :append-icon="show ? this.mdiEye : this.mdiEyeOff"
                    @click:append="show = !show"
                  ></v-text-field>
                </ValidationProvider>
              </v-form>
            </ValidationObserver>
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
</template>

<script lang="ts">
import Vue from "vue";
import { mdiEye, mdiEyeOff, mdiAccount, mdiKey, mdiLogin } from "@mdi/js";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

extend("required", {
  ...required,
  message: "{_field_} is required"
});

export default Vue.extend({
  components: { ValidationObserver, ValidationProvider },

  data() {
    return {
      mdiEye: mdiEye,
      mdiEyeOff: mdiEyeOff,
      mdiAccount: mdiAccount,
      mdiKey: mdiKey,
      mdiLogin: mdiLogin,

      show: false,

      username: "",
      password: ""
    };
  },

  methods: {
    login: function() {
      this.$refs.observer.validate().then((valid: boolean) => {
        if (valid) {
          const { username, password } = this;
          this.$store
            .dispatch("auth/AUTH_LOGIN", { username, password })
            .then(() => {
              this.$router.push("/");
            })
            .catch((err: any) => {
              console.log(err);
            });
        }
      });
    }
  }
});
</script>