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
          <v-text-field
            type="number"
            v-model="inputNumber"
            label="Number"
            color="orange accent-2"
            required
          ></v-text-field>
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

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },
    number: { type: String, required: true }
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