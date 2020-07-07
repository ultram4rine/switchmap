<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>New floor</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="$emit('close')">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <v-text-field
            type="number"
            v-model="floorNumber"
            label="Number"
            color="orange accent-2"
            required
          ></v-text-field>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('submit')">Add</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { mdiClose } from "@mdi/js";

import mixins from "vue-typed-mixins";

import floorsMixin from "../../mixins/floorsMixin";

export default mixins(floorsMixin).extend({
  props: {
    form: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose
    };
  }
});
</script>