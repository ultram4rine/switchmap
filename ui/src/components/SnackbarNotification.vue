<template>
  <v-snackbar
    :value="snackbar"
    :timeout="3000"
    :color="type"
    @input="close"
    left
    bottom
  >
    {{ text }}
    <template v-slot:action="{ attrs }">
      <v-btn fab x-small v-bind="attrs" @click="close">
        <v-icon dark>{{ mdiClose }}</v-icon>
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { mdiClose } from "@mdi/js";

export default defineComponent({
  props: {
    snackbar: { type: Boolean, required: true },
    type: {
      type: String,
      required: true,
      enum: ["success", "info", "warning", "error"],
    },
    text: { type: String, required: true },
  },

  setup(props, { emit }) {
    const close = () => {
      emit("close");
    };

    return {
      close,

      mdiClose,
    };
  },
});
</script>
