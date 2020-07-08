<template>
  <div id="upload">
    <v-form>
      <div class="title mb-1">Can't find plan for with floor, please upload</div>
      <v-file-input
        show-size
        label="File input"
        accept=".png, .jpg, .jpeg"
        color="orange darken-1"
        required
      ></v-file-input>
      <v-btn dark color="orange darken-1" class="mr-4" @click="upload">Upload</v-btn>
    </v-form>
  </div>
</template>

<script lang="ts">
import Vue from "vue";

export default Vue.extend({});
</script>