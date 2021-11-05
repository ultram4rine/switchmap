<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader
          class="mx-auto"
          type="card-heading, list-item, actions"
        ></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="b in builds"
        :key="b.shortName"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <build-card
          :build="b"
          @handleEdit="openBuildForm('Edit', b)"
          @handleDelete="handleDelete"
          @handleAddFloor="openFloorForm(b.shortName)"
        />
      </v-col>

      <v-col
        v-if="!isLoading && builds.length == 0"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <v-card class="ma-1" outlined>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">
                No builds to show
              </v-list-item-title>
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

    <build-form
      :form="buildForm"
      :action="buildFormAction"
      :build="build"
      @submit="handleSubmitBuild"
      @close="closeBuildForm"
    />

    <floor-form
      :form="floorForm"
      :floor="floor"
      @submit="handleSubmitFloor"
      @close="closeFloorForm"
    />

    <delete-confirmation
      :confirmation="deleteConfirmation"
      :name="deleteItemName"
      @confirm="deleteConfirm"
      @cancel="deleteCancel"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";

import BuildCard from "../components/cards/BuildCard.vue";
import DeleteConfirmation from "../components/DeleteConfirmation.vue";
import BuildForm from "../components/forms/BuildForm.vue";
import FloorForm from "../components/forms/FloorForm.vue";

import { BuildRequest, BuildResponse } from "../types/build";
import { FloorRequest } from "../types/floor";
import { getBuilds, addBuild, editBuild, deleteBuild } from "../api/builds";
import { addFloor } from "../api/floors";

import useBuildForm from "@/composables/useBuildForm";
import useFloorForm from "@/composables/useFloorForm";
import useDeleteConfirmation from "@/composables/useDeleteConfirmation";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    BuildCard,
    DeleteConfirmation,
    BuildForm,
    FloorForm,
  },

  setup() {
    const builds: Ref<BuildResponse[]> = ref([]);
    const buildShortName = ref("");

    const {
      form: buildForm,
      formAction: buildFormAction,
      build,
      oldBuildShortName,
      openForm: openBuildForm,
      submitForm: submitBuildForm,
      closeForm: closeBuildForm,
    } = useBuildForm();

    const {
      form: floorForm,
      floor,
      buildShortName: floorBuildShortName,
      openForm: openFloorForm,
      submitForm: submitFloorForm,
      closeForm: closeFloorForm,
    } = useFloorForm();

    const { deleteConfirmation, deleteItemName } = useDeleteConfirmation();

    return {
      builds,
      buildShortName,

      // Build form.
      buildForm,
      buildFormAction,
      build,
      oldBuildShortName,
      openBuildForm,
      submitBuildForm,
      closeBuildForm,

      // Floor form.
      floorForm,
      floor,
      floorBuildShortName,
      openFloorForm,
      submitFloorForm,
      closeFloorForm,

      // Delete confirmation.
      deleteConfirmation,
      deleteItemName,

      deleteBuild,
    };
  },

  methods: {
    displayBuilds() {
      getBuilds().then((builds) => (this.builds = builds));
    },

    handleDelete(b: BuildResponse) {
      this.deleteItemName = b.name;
      this.buildShortName = b.shortName;
      this.deleteConfirmation = true;
    },

    deleteConfirm() {
      deleteBuild(this.buildShortName)
        .then(() => {
          this.deleteConfirmation = false;
          this.deleteItemName = "";
          this.shortName = "";
        })
        .then(() => this.displayBuilds());
    },

    deleteCancel() {
      this.deleteConfirmation = false;
      this.deleteItemName = "";
      this.shortName = "";
    },

    handleSubmitBuild(name: string, shortName: string, action: "Add" | "Edit") {
      this.submitBuildForm(name, shortName, action, this.displayBuilds);
    },

    handleSubmitFloor(number: number) {
      this.submitFloorForm(number, this.displayBuilds);
    },
  },

  created() {
    this.displayBuilds();
  },
});
</script>
