<template>
  <v-card class="ma-1" outlined>
    <v-card-title class="headline">
      <div class="ellipsis">{{ build.name }}</div>
    </v-card-title>

    <v-card-subtitle>
      {{ build.floorsNumber }}
      {{ build.floorsNumber !== 1 ? "floors" : "floor" }},
      {{ build.switchesNumber }}
      {{ build.switchesNumber !== 1 ? "switches" : "switch" }}
    </v-card-subtitle>

    <v-card-actions>
      <v-btn
        class="white--text"
        size="small"
        color="primary"
        @click="handleAddFloor"
      >
        Add floor
      </v-btn>
      <v-btn
        class="white--text"
        size="small"
        color="primary"
        :to="{ name: 'build', params: { shortName: build.shortName } }"
      >
        Go
      </v-btn>
      <v-spacer></v-spacer>
      <v-btn
        size="small"
        color="grey"
        :icon="mdiPencil"
        @click="handleEdit"
      ></v-btn>
      <v-btn
        size="small"
        color="red"
        :icon="mdiDelete"
        @click="handleDelete"
      ></v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { mdiPencil, mdiDelete } from "@mdi/js";

import { BuildResponse } from "@/interfaces/build";

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

<style scoped>
.v-card-title {
  flex-wrap: nowrap;
}
.ellipsis {
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}
</style>
