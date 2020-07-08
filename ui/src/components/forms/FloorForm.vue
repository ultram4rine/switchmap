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
            v-model="inputNumber.input"
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
import { defineComponent } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import useInputValidator from "@/helpers/useInputValidator";

export default defineComponent({
  name: "FloorForm",

  props: {
    form: { type: Boolean, required: true },
    number: { type: Number, required: true }
  },

  setup(props, { emit }) {
    const inputNumber = useInputValidator(
      props.number.toString(),
      [],
      (number: string) => emit("input", name)
    );

    return {
      inputNumber,

      mdiClose
    };
  }
});
</script>