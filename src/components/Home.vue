<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" :cols="3">
        <v-skeleton-loader type="article" class="mx-auto"></v-skeleton-loader>
      </v-col>
      <v-col v-else v-for="build in builds" :key="build.addr" :cols="3">
        <v-card class="ma-1" outlined>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">{{ build.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ build.floors }} floors, {{ build.switches }} switches</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              small
              color="primary"
              @click="addFloorForm = !addFloorForm; floorBuildName = build.name; floorBuildAddr = build.addr"
            >Add floor</v-btn>
            <v-btn small color="primary" :to="{ name: 'build', params: { addr: build.addr }}">Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-card class="ma-1">
        <v-btn color="error" @click="addBuildForm = !addBuildForm">Add build</v-btn>
      </v-card>
    </v-row>

    <v-snackbar v-model="snackbar" :timeout="timeout">
      {{ item }} added!
      <v-btn color="orange darken-3" text @click="snackbar = false">Close</v-btn>
    </v-snackbar>

    <v-dialog :value="addBuildForm" max-width="500px">
      <v-card dark>
        <v-toolbar>
          <v-toolbar-title>New build</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="addBuildForm = false; buildName = ''; buildAddr = ''">
            <v-icon>{{ mdiClose }}</v-icon>
          </v-btn>
        </v-toolbar>
        <v-card-text>
          <v-form ref="form">
            <v-text-field v-model="buildName" label="Name" color="orange accent-2" required></v-text-field>
            <v-text-field v-model="buildAddr" label="Address" color="orange accent-2" required></v-text-field>
          </v-form>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="orange darken-1" @click="addBuild">Add</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="addFloorForm" max-width="500px">
      <v-card dark>
        <v-toolbar>
          <v-toolbar-title>New floor</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn
            icon
            @click="addFloorForm = false; floorNumber = ''; floorBuildName = ''; floorBuildAddr = ''"
          >
            <v-icon>{{ mdiClose }}</v-icon>
          </v-btn>
        </v-toolbar>
        <v-card-text>
          <v-form ref="form">
            <v-text-field
              type="number"
              v-model="floorNumber"
              label="Number"
              color="orange accent-2"
              required
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="orange darken-1" @click="addFloor">Add</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import axios from "axios";
import { mdiClose } from "@mdi/js";

import { Build, Builds } from "../types";

export default Vue.extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose,

      snackbar: false,
      timeout: 3000,

      item: "",

      builds: [] as Array<Build>,
      buildsEndpoint: "http://localhost:8080/builds",

      addBuildForm: false,
      addBuildEndpoint: "http://localhost:8080/build",
      buildName: "",
      buildAddr: "",

      addFloorForm: false,
      addFloorFormEndpoint: "http://localhost:8080/floor",
      floorNumber: "",
      floorBuildName: "",
      floorBuildAddr: ""
    };
  },

  created() {
    this.getAllBuilds();
  },

  methods: {
    getAllBuilds() {
      axios
        .get<Builds>(this.buildsEndpoint, {
          transformResponse: (r: any) => r.data
        })
        .then(resp => (this.builds = resp.data.builds))
        .catch(err => console.log(err));
    },

    addBuild() {
      axios
        .post(this.addBuildEndpoint, {
          name: this.buildName,
          addr: this.buildAddr
        })
        .then(() => {
          this.getAllBuilds();
          this.addBuildForm = false;
          this.item = this.buildName;
          this.snackbar = true;
        })
        .catch(err => console.log(err));
    },

    addFloor() {
      axios
        .post(this.addFloorFormEndpoint, {
          number: parseInt(this.floorNumber, 10),

          buildName: this.floorBuildName,
          buildAddr: this.floorBuildAddr
        })
        .then(() => {
          this.addFloorForm = false;
          this.item = `${this.floorNumber} floor in ${this.buildName}`;
          this.snackbar = true;
        })
        .catch(err => console.log(err));
    }
  }
});
</script>