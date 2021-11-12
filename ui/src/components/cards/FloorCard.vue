<template>
  <v-card class="ma-1" outlined>
    <v-card-title class="headline">
      Floor {{ floor.number }}
      <v-spacer></v-spacer>
      <v-btn icon small color="red" @click="handleDelete">
        <v-icon>{{ mdiDelete }}</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-subtitle>
      {{ floor.switchesNumber }}
      {{ floor.switchesNumber !== 1 ? "switches" : "switch" }}
    </v-card-subtitle>

    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn dark small color="primary" @click="handleAddSwitch">
        Add switch
      </v-btn>
      <v-btn
        dark
        small
        color="primary"
        :to="{
          name: 'floor',
          params: { build: shortName, floor: floor.number.toString() },
        }"
      >
        Go
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, PropType } from "@vue/composition-api";
import { mdiDelete } from "@mdi/js";

import { FloorResponse } from "@/types/floor";

export default defineComponent({
  props: {
    shortName: { type: String, required: true },
    floor: { type: Object as PropType<FloorResponse>, required: true },
  },
  setup() {
    return { mdiDelete };
  },
  methods: {
    handleDelete() {
      this.$emit("handleDelete", this.floor);
    },
    handleAddSwitch() {
      this.$emit("handleAddSwitch", this.shortName, this.floor);
    },
  },
});
</script>
