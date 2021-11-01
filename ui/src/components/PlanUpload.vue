<template>
  <v-form>
    <v-banner>Can't find plan for with floor, please upload</v-banner>
    <v-file-input
      v-model="plan"
      show-size
      label="File input"
      accept=".png, .jpg, .jpeg"
      color="orange darken-1"
      required
    ></v-file-input>
    <v-btn dark color="orange darken-1" class="mr-4" @click="upload">
      Upload
    </v-btn>
  </v-form>
</template>

<script lang="ts">
import { defineComponent, ref } from "@vue/composition-api";
export default defineComponent({
  setup() {
    const plan = ref({} as File);

    return {
      plan,
    };
  },

  methods: {
    upload() {
      this.$emit("upload", this.plan);
    },
  },
});
</script>
