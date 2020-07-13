<template>
  <v-snackbar :value="snackbar" :timeout="timeout" @input="close">
    {{ selfItem }} {{ selfAction }}
    <template v-slot:action="{ attrs }">
      <v-btn fab x-small v-bind="attrs" @click="close">
        <v-icon dark>{{ mdiClose }}</v-icon>
      </v-btn>
    </template>
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
      mdiClose: mdiClose,

      timeout: 3000,

      selfItem: this.item,
      selfAction: this.action
    };
  },

  watch: {
    item: function(newItem) {
      this.selfItem = newItem;
    },
    action: function(newAction) {
      this.selfAction = newAction;
    }
  },

  methods: {
    close() {
      this.$emit("update", false);
    }
  }
});
</script>