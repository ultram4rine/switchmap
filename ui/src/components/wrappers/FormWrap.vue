<template>
  <v-row justify="center">
    <v-dialog v-model="form" persistent max-width="500px" width="500px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ title }}</span>
        </v-card-title>
        <slot />
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    title: { type: String, required: true },
  },
});
</script>
