<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" :sm="12" :md="4" :lg="2">
        <v-skeleton-loader type="article" class="mx-auto"></v-skeleton-loader>
      </v-col>
      <v-col v-else v-for="build in builds" :key="build.addr" :sm="12" :md="4" :lg="2">
        <v-card class="ma-1" outlined>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">{{ build.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ build.floorsNumber }} floors, {{ build.switchesNumber }} switches</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="addFloorForm = !addFloorForm; floorBuildName = build.name; floorBuildAddr = build.addr"
            >Add floor</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'build', params: { addr: build.addr }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && builds.length===0" :sm="12" :md="4" :lg="2">
        <v-card class="ma-1" outlined>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">No builds to show</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
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
      <template v-slot:action="{ attrs }">
        <v-btn fab x-small v-bind="attrs" @click="snackbar = false">
          <v-icon dark>{{ mdiClose }}</v-icon>
        </v-btn>
      </template>
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
          <v-btn color="orange darken-1" @click="addFloorWithRefresh">Add</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import mixins from "vue-typed-mixins";
import { mdiClose } from "@mdi/js";

import { config } from "../config";

import buildsMixin from "../mixins/buildsMixin";
import floorsMixin from "../mixins/floorsMixin";

export default mixins(buildsMixin, floorsMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose
    };
  },

  created() {
    this.getAllBuilds();
  },

  methods: {
    addFloorWithRefresh() {
      this.addFloor();
      this.getBuild(this.floorBuildAddr);
      this.floorBuildAddr = "";
    }
  }
});
</script>