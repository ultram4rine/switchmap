<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader class="mx-auto" type="card-heading, list-item, actions"></v-skeleton-loader>
      </v-col>
      <v-col v-else v-for="b in builds" :key="b.shortName" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-card class="ma-1" outlined>
          <v-card-title class="headline">
            {{ b.name }}
            <v-spacer></v-spacer>
            <v-btn icon small color="grey" @click="openBuildForm('Change', b)">
              <v-icon>{{ mdiPencil }}</v-icon>
            </v-btn>
            <v-btn
              icon
              small
              color="red"
              @click="buildForDeleteName = b.name; buildForDeleteShortName = b.shortName; confirmation = !confirmation"
            >
              <v-icon>{{ mdiDelete }}</v-icon>
            </v-btn>
          </v-card-title>

          <v-card-subtitle>{{ b.floorsNumber }} floors, {{ b.switchesNumber }} switches</v-card-subtitle>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="floorBuildShortName = b.shortName; floorForm = !floorForm"
            >Add floor</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'build', params: { build: b.shortName }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && builds.length == 0" cols="12" sm="6" md="4" lg="3" xl="2">
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
        <v-btn color="error" @click="openBuildForm('Add')">Add build</v-btn>
      </v-card>
    </v-row>

    <BuildForm
      :form="buildForm"
      :action="buildAction"
      :name="buildName"
      :shortName="buildShortName"
      @submit="handleSubmitBuild"
      @close="closeBuildForm"
    />

    <FloorForm
      :form="floorForm"
      :number="floorNumber"
      @submit="handleSubmitFloorWithRefresh"
      @close="closeFloorForm"
    />

    <Confirmation
      :confirmation="confirmation"
      :name="'build ' + buildForDeleteName"
      @confirm="deleteBuild(buildForDeleteShortName)"
      @cancel="confirmation = !confirmation"
    />

    <Snackbar :snackbar="snackbar" :item="item" :action="action" @update="updateSnackbar" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "@vue/composition-api";
import { mdiPencil, mdiDelete } from "@mdi/js";

import BuildForm from "@/components/forms/BuildForm.vue";
import FloorForm from "@/components/forms/FloorForm.vue";

import Confirmation from "@/components/Confirmation.vue";
import Snackbar from "@/components/Snackbar.vue";

import useBuilds from "@/helpers/useBuilds";
import useFloors from "@/helpers/useFloors";
import useConfirmation from "@/helpers/useConfirmation";
import useSnackbar from "@/helpers/useSnackbar";

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
    const {
      builds,
      buildForm,
      buildAction,
      buildName,
      buildShortName,
      openBuildForm,
      handleSubmitBuild,
      closeBuildForm,
      buildForDeleteName,
      buildForDeleteShortName,
      buildError,
      getAllBuilds,
      getBuild,
      addBuild,
      updateBuild,
      deleteBuild
    } = useBuilds();

    const {
      floorForm,
      floorNumber,
      floorBuildShortName,
      closeFloorForm,
      addFloorTo
    } = useFloors();

    const handleSubmitFloorWithRefresh = (number: string) => {
      addFloorTo(floorBuildShortName.value, parseInt(number)).then(() => {
        getBuild(floorBuildShortName.value).then(() => {
          const i = builds.value.findIndex(
            b => b.shortName === floorBuildShortName.value
          );
          builds.value[i] = build.value;
          closeFloorForm();
        });
      });
    };

    const { confirmation, name } = useConfirmation();
    const { snackbar, item, action, updateSnackbar } = useSnackbar();

    return {
      builds,

      buildForm,
      buildAction,
      buildName,
      buildShortName,

      openBuildForm,
      handleSubmitBuild,
      closeBuildForm,

      buildForDeleteName,
      buildForDeleteShortName,

      getAllBuilds,
      addBuild,
      updateBuild,
      deleteBuild,

      floorForm,
      floorNumber,
      floorBuildShortName,

      handleSubmitFloorWithRefresh,
      closeFloorForm,

      addFloorTo,

      confirmation,
      name,

      snackbar,
      item,
      action,
      updateSnackbar,

      mdiPencil,
      mdiDelete
    };
  },

  created() {
    this.getAllBuilds().then(builds => (this.builds = builds));
  }
});
</script>