<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        <v-toolbar-title>Switches</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-dialog v-model="dialog" max-width="500px">
          <template v-slot:activator="{ on }">
            <v-btn color="orange darken-1" dark v-on="on">Add switch</v-btn>
          </template>
          <v-card dark>
            <v-toolbar>
              <v-toolbar-title>New switch</v-toolbar-title>
              <v-spacer></v-spacer>
              <v-btn icon @click="dialog = false">
                <v-icon>{{ mdiClose }}</v-icon>
              </v-btn>
            </v-toolbar>

            <v-card-text>
              <v-container>
                <v-row></v-row>
              </v-container>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="orange darken-1" @click="dialog = false">Add</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          :append-icon="this.mdiMagnify"
          label="Search"
          color="orange accent-2"
          single-line
          hide-details
        ></v-text-field>
      </v-toolbar>
      <v-data-table
        :headers="headers"
        :items="switches"
        :items-per-page="10"
        :search="search"
        sort-by="name"
        multi-sort
        class="elevation-1"
      ></v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { mdiClose, mdiMagnify } from "@mdi/js";

export default Vue.extend({
  data() {
    return {
      mdiClose: mdiClose,
      mdiMagnify: mdiMagnify,

      dialog: false,

      search: "",

      switches: [
        {
          name: "switch",
          mac: "00:30:48:5a:58:65",
          ip: "192.168.1.1",
          serial: "AAAAAAAAA",
          location: "name build number floor"
        }
      ],
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