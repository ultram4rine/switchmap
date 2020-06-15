<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" :cols="3">
        <v-skeleton-loader type="article" class="mx-auto"></v-skeleton-loader>
      </v-col>
      <v-col v-else v-for="floor in floors" :key="floor.number" :cols="3">
        <v-card class="ma-1" outlined>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">Floor {{ floor.number }}</v-list-item-title>
              <v-list-item-subtitle>{{ floor.switchesNumber }} switches</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="addSwitchForm = !addSwitchForm; switchFloorNumber = floor.number"
            >Add switch</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'floor', params: { addr: floor.number }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && floors.length===0" :cols="3">
        <v-card class="ma-1" outlined>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">No floors to show</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-card class="ma-1">
        <v-btn color="error" @click="addFloorForm = !addFloorForm">Add floor</v-btn>
      </v-card>
    </v-row>

    <v-snackbar v-model="snackbar" :timeout="timeout">
      {{ item }} added!
      <v-btn fab x-small @click="snackbar = false">
        <v-icon dark>{{ mdiClose }}</v-icon>
      </v-btn>
    </v-snackbar>

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
import axios, { AxiosResponse } from "axios";
import { mdiClose } from "@mdi/js";

import config from "../config/config";
import { Floor } from "../interfaces";

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

      floors: new Array<Floor>(),
      floorsEndpoint: `${config.apiURL}/build/${this.$route.params.addr}/floors`,

      addFloorForm: false,
      addFloorFormEndpoint: `${config.apiURL}/floor`,
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
        .get<Floor, AxiosResponse<Floor[]>>(this.floorsEndpoint)
        .then(resp => (this.floors = resp.data))
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
          this.item = `${this.floorNumber} floor`;
          this.snackbar = true;
        })
        .catch(err => console.log(err));
    }
  }
});
</script>