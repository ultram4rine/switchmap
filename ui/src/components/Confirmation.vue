<template>
  <v-dialog :value="value" persistent max-width="500px">
    <v-card dark>
      <v-card-title class="headline">Delete {{ name }}?</v-card-title>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('cancel')">No</v-btn>
        <v-btn color="red" @click="$emit('confirm')">Yes</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";

export default Vue.extend({
  props: {
    value: { type: Boolean, required: true },
    name: { type: String, required: true }
  }
});
</script>