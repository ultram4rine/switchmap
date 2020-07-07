<template>
  <v-snackbar v-model="snackbar" :timeout="timeout">
    {{ item }} {{ action }}
    <v-btn fab x-small @click="$emit('closeSnackbar')">
      <v-icon dark>{{ mdiClose }}</v-icon>
    </v-btn>
  </v-snackbar>
</template>

<script lang="ts">
import { mdiClose } from "@mdi/js";

import Vue from "vue";

export default Vue.extend({
  props: {
    snackbar: { type: Boolean, required: true },
    item: { type: String, required: true },
    action: { type: String, required: true }
  },

  data() {
    return {
      timeout: 3000
    };
  }
});
</script>