<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader class="mx-auto" type="card-heading, list-item, actions"></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="build in builds"
        :key="build.shortName"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <v-card class="ma-1" outlined>
          <v-card-title class="headline">
            {{ build.name }}
            <v-spacer></v-spacer>
            <v-btn
              icon
              small
              color="grey"
              @click="action = 'Change'; buildName = build.name; buildShortName = build.shortName; buildForm = !buildForm"
            >
              <v-icon>{{ mdiPencil }}</v-icon>
            </v-btn>
            <v-btn
              icon
              small
              color="red"
              @click="buildForDeleteName = build.name; buildForDeleteShortName = build.shortName; confirmation = !confirmation"
            >
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
              @click="floorBuildName = build.name; floorBuildShortName = build.shortName; floorForm = !floorForm"
            >Add floor</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'build', params: { build: build.shortName }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && builds.length===0" cols="12" sm="6" md="4" lg="3" xl="2">
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

    <BuildForm
      :form="buildForm"
      :action="action"
      :name="buildName"
      :shortName="buildShortName"
      @submit="handleSubmitBuild"
      @close="closeBuildForm"
    />

    <FloorForm
      :form="floorForm"
      :number="floorNumber"
      @submit="handleSubmitFloor"
      @close="closeFloorForm"
    />

    <Confirmation
      v-model="confirmation"
      item="build"
      :name="buildForDeleteName"
      @confirm="deleteBuild(buildForDeleteShortName)"
      @cancel="confirmation = !confirmation"
    />

    <Snackbar :snackbar="snackbar" :item="item" :action="snackbarAction" @close="closeSnackbar" />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiClose, mdiPencil, mdiDelete } from "@mdi/js";

import buildsMixin from "../mixins/buildsMixin";
import floorsMixin from "../mixins/floorsMixin";

import BuildForm from "./forms/BuildForm.vue";
import FloorForm from "./forms/FloorForm.vue";

import Confirmation from "./Confirmation.vue";
import Snackbar from "./Snackbar.vue";

export default mixins(buildsMixin, floorsMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  components: {
    BuildForm,
    FloorForm,
    Confirmation,
    Snackbar
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
    handleSubmitBuild(name: string, shortName: string) {
      if (this.action == "Add") {
        this.buildName = name;
        this.buildShortName = shortName;
        this.addBuild();
      } else if (this.action == "Change") {
        this.buildForUpdate = this.buildShortName;
        this.buildName = name;
        this.buildShortName = shortName;
        this.updateBuild(this.buildForUpdate);
      }
    },
    handleSubmitFloor(number: string) {
      this.floorNumber = number;
      this.addFloor();
      this.getBuild(this.floorBuildShortName);
      this.floorBuildShortName = "";
    }
  }
});
</script>