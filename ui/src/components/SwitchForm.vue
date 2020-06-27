<template>
  <v-dialog :value="value" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>New switch</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="$emit('close')">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-container>
          <v-row></v-row>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('submit')">Add</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";

import { mdiClose } from "@mdi/js";

export default Vue.extend({
  props: {
    value: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose
    };
  },

  methods: {}
});
</script>