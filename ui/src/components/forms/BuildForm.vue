<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="$emit('close')">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <v-text-field
            v-model="buildName"
            label="Name"
            color="orange accent-2"
            required
            v-on:keyup="emitBuildName"
          ></v-text-field>
          <v-text-field
            v-model="buildShortName"
            label="Short Name"
            color="orange accent-2"
            required
            v-on:keyup="emitBuildShortName"
          ></v-text-field>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('submit')">{{ action }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";

import { mdiClose } from "@mdi/js";

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose,

      buildName: "",
      buildShortName: ""
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New build";
      else if (this.action == "Change") return "Change build";
    }
  },

  methods: {
    emitBuildName() {
      this.$emit("emitBuildName", this.buildName);
    },
    emitBuildShortName() {
      this.$emit("emitBuildShortName", this.buildShortName);
    }
  }
});
</script>