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
              @click="buildForm = !buildForm; buildName = build.name; buildShortName = build.shortName; action = 'Change'"
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
              @click="floorForm = !floorForm; floorBuildName = build.name; floorBuildShortName = build.shortName"
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

    <Snackbar :snackbar="snackbar" :item="item" :action="snackbarAction" @update="updateSnackbar" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";
import { mdiClose, mdiPencil, mdiDelete } from "@mdi/js";

import { Build } from "@/interfaces";
import { getAllBuilds as getAllBuildsAPI } from "@/api/build";

import BuildForm from "@/components/forms/BuildForm.vue";
import FloorForm from "@/components/forms/FloorForm.vue";

import Confirmation from "@/components/Confirmation.vue";
import Snackbar from "@/components/Snackbar.vue";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  components: {
    BuildForm,
    FloorForm,
    Confirmation,
    Snackbar
  },

  setup() {
    const builds: Ref<Build[]> = ref([]);

    const getAllBuilds = () => {
      builds.value = getAllBuildsAPI();
    };

    const buildForm = ref(false);
    const buildName = ref("");
    const buildShortName = ref("");

    const floorForm = ref(false);
    const floorNumber = ref(0);

    return {
      builds,
      getAllBuilds,

      buildForm,
      buildName,
      buildShortName,

      floorForm,
      floorNumber,

      mdiClose,
      mdiPencil,
      mdiDelete
    };
  },

  created() {
    this.getAllBuilds();
  }
});
</script>