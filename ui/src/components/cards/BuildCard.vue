<template>
  <v-card class="ma-1" outlined>
    <v-card-title class="headline">
      {{ build.name }}
      <v-spacer></v-spacer>
      <v-btn icon small color="grey" @click="handleEdit">
        <v-icon>{{ mdiPencil }}</v-icon>
      </v-btn>
      <v-btn icon small color="red" @click="handleDelete">
        <v-icon>{{ mdiDelete }}</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-subtitle>
      {{ build.floorsNumber }}
      {{ build.floorsNumber !== 1 ? "floors" : "floor" }},
      {{ build.switchesNumber }}
      {{ build.switchesNumber !== 1 ? "switches" : "switch" }},
    </v-card-subtitle>

    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn dark small color="primary" @click="handleAddFloor">
        Add floor
      </v-btn>
      <v-btn
        dark
        small
        color="primary"
        :to="{ name: 'build', params: { shortName: build.shortName } }"
      >
        Go
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, PropType } from "@vue/composition-api";
import { mdiPencil, mdiDelete } from "@mdi/js";

import { BuildResponse } from "../../types/build";

export default defineComponent({
  props: {
    build: { type: Object as PropType<BuildResponse>, required: true },
  },
  setup() {
    return { mdiPencil, mdiDelete };
  },
  methods: {
    handleEdit() {
      this.$emit("handleEdit", this.build);
    },
    handleDelete() {
      this.$emit("handleDelete", this.build);
    },
    handleAddFloor() {
      this.$emit("handleAddFloor", this.build);
    },
  },
});
</script>
