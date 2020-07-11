<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
            <v-text-field
              v-model="inputName"
              :error-messages="errors"
              label="Name"
              required
              color="orange accent-2"
            ></v-text-field>
          </ValidationProvider>

          <ValidationProvider v-slot="{ errors }" name="Short name" rules="required">
            <v-text-field
              v-model="inputShortName"
              :error-messages="errors"
              label="Short name"
              required
              color="orange accent-2"
            ></v-text-field>
          </ValidationProvider>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="submit">{{ action }}</v-btn>
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
  message: "{_field_} is required"
});

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },

    name: { type: String, required: true },
    shortName: { type: String, required: true }
  },

  components: {
    ValidationProvider
  },

  data() {
    return {
      mdiClose: mdiClose,

      inputName: this.name,
      inputShortName: this.shortName
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New build";
      else if (this.action == "Change") return "Change build";
    }
  },

  watch: {
    name: function(newName) {
      this.inputName = newName;
    },
    shortName: function(newShortName) {
      this.inputShortName = newShortName;
    }
  },

  methods: {
    submit() {
      this.$emit("submit", this.inputName, this.inputShortName);
    },
    close() {
      this.$emit("close");
    }
  }
});
</script>