<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        Switches
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          :append-icon="this.mdiMagnify"
          label="Search"
          color="orange darken-1"
          single-line
          hide-details
        ></v-text-field>
      </v-toolbar>
      <v-data-table
        :headers="headers"
        :items-per-page="10"
        :search="search"
        multi-sort
        class="elevation-1"
      ></v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { mdiMagnify } from "@mdi/js";

export default Vue.extend({
  data() {
    return {
      mdiMagnify: mdiMagnify,

      search: "",

      headers: [
        {
          text: "Name",
          align: "start",
          value: "name"
        },
        { text: "MAC", value: "mac" },
        { text: "IP", value: "ip" },
        { text: "Serial", value: "serial" },
        { text: "Location", value: "location" }
      ]
    };
  }
});
</script>