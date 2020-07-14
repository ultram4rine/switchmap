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

    <Confirmation
      v-model="confirmation"
      :name="'build' + buildForDeleteName"
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

import BuildForm from "@/components/forms/BuildForm.vue";
import FloorForm from "@/components/forms/FloorForm.vue";

import Confirmation from "@/components/Confirmation.vue";
import Snackbar from "@/components/Snackbar.vue";

import useBuilds from "@/helpers/useBuilds";
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
      buildName,
      buildShortName,
      buildError,
      getAllBuilds
    } = useBuilds();

    const floorForm = ref(false);
    const floorNumber = ref(0);

    const { value, name } = useConfirmation();
    const { snackbar, action, item } = useSnackbar();

    return {
      builds,

      buildForm,
      buildName,
      buildShortName,

      getAllBuilds,

      floorForm,
      floorNumber,

      value,
      name,

      snackbar,
      action,
      item,

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