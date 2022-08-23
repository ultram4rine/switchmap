<template>
  <v-form>
    <v-banner v-if="!update">
      Can't find plan for with floor, please upload
    </v-banner>
    <v-file-input
      v-model="plan"
      show-size
      label="File input"
      accept=".png, .jpg, .jpeg"
      color="orange darken-1"
      required
    ></v-file-input>
    <v-btn
      color="orange darken-1"
      class="mr-4 white--text"
      :disabled="!plan.size"
      @click="upload"
    >
      Upload
    </v-btn>
    <v-btn
      v-if="update"
      color="orange darken-1"
      class="mr-4 white--text"
      @click="cancel"
    >
      Cancel
    </v-btn>
  </v-form>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
export default defineComponent({
  props: {
    update: { type: Boolean, required: true },
  },

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
    cancel() {
      this.$emit("cancel");
    },
  },
});
</script>
