<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>New floor</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <ValidationProvider v-slot="{ errors }" name="Number" rules="required">
            <v-text-field
              v-model="inputNumber"
              :error-messages="errors"
              label="Number"
              required
              color="orange accent-2"
            ></v-text-field>
          </ValidationProvider>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="submit">Add</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";
import { mdiClose } from "@mdi/js";

import { ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

extend("required", {
  ...required,
  message: "{_field_} can not be empty"
});

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },

    number: { type: String, required: true }
  },

  components: {
    ValidationProvider
  },

  data() {
    return {
      mdiClose: mdiClose,

      inputNumber: this.number
    };
  },

  methods: {
    submit() {
      this.$emit("submit", this.inputNumber);
    },
    close() {
      this.$emit("close");
    }
  }
});
</script>