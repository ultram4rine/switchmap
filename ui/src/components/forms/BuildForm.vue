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
          <v-text-field v-model="buildName" label="Name" color="orange accent-2" required></v-text-field>
          <v-text-field
            v-model="buildShortName"
            label="Short Name"
            color="orange accent-2"
            required
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
import { mdiClose } from "@mdi/js";

import mixins from "vue-typed-mixins";

import buildsMixin from "../../mixins/buildsMixin";

export default mixins(buildsMixin).extend({
  props: {
    form: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New build";
      else if (this.action == "Change") return "Change build";
    }
  }
});
</script>