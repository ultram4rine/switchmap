<template>
  <v-dialog :value="form" persistent max-width="500px" width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>
      <slot />
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    title: { type: String, required: true },
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
