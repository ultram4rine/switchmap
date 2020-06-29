<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="12" md="4" lg="3" xl="2">
        <v-skeleton-loader class="mx-auto" type="card-heading, list-item, actions"></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="build in builds"
        :key="build.addr"
        cols="12"
        sm="12"
        md="4"
        lg="3"
        xl="2"
      >
        <v-card class="ma-1" outlined>
          <v-card-title class="headline">
            {{ build.name }}
            <v-spacer></v-spacer>
            <v-btn icon small color="grey">
              <v-icon>{{ mdiPencil }}</v-icon>
            </v-btn>
            <v-btn icon small color="red">
              <v-icon>{{ mdiDelete }}</v-icon>
            </v-btn>
          </v-card-title>

          <v-card-subtitle>{{ build.floorsNumber }} floors, {{ build.switchesNumber }} switches</v-card-subtitle>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="floorForm = !floorForm; floorBuildName = build.name; floorBuildAddr = build.addr"
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

      <v-col v-if="!isLoading && builds.length===0" cols="12" sm="12" md="4" lg="3" xl="2">
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
        <v-btn color="error" @click="buildForm = !buildForm">Add build</v-btn>
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

    <BuildForm
      v-model="buildForm"
      @submit="addBuild"
      @close="closeBuildForm"
      v-on:emitBuildName="updateBuildName"
      v-on:emitBuildAddr="updateBuildAddr"
    />

    <FloorForm
      v-model="floorForm"
      @submit="addFloorWithRefresh"
      @close="closeFloorForm"
      v-on:emitFloorNumber="updateFloorNumber"
    />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiClose, mdiPencil, mdiDelete } from "@mdi/js";

import buildsMixin from "../mixins/buildsMixin";
import floorsMixin from "../mixins/floorsMixin";

import BuildForm from "./BuildForm.vue";
import FloorForm from "./FloorForm.vue";

export default mixins(buildsMixin, floorsMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  components: {
    BuildForm,
    FloorForm
  },

  data() {
    return {
      mdiClose: mdiClose,
      mdiPencil: mdiPencil,
      mdiDelete: mdiDelete
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