<template>
  <div id="home">
    <v-row no-gutters dense>
      <v-col
        v-if="isLoading && builds.length === 0"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
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
      <v-card :style="{ visibility: 'hidden' }" class="ma-1">
        <v-btn rounded large></v-btn>
      </v-card>
    </v-row>

    <v-btn
      rounded
      large
      fixed
      bottom
      right
      color="orange accent-4"
      class="white--text"
      @click="openBuildForm('Add')"
    >
      Add build
    </v-btn>

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
      @confirm="confirmDelete"
      @cancel="deleteCancel(() => (buildShortName = ''))"
    />

    <snackbar-notification
      :snackbar="snackbar"
      :type="snackbarType"
      :text="snackbarText"
      @close="closeSnackbar"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";

import BuildCard from "@/components/cards/BuildCard.vue";
import BuildForm from "@/components/forms/BuildForm.vue";
import FloorForm from "@/components/forms/FloorForm.vue";
import DeleteConfirmation from "@/components/DeleteConfirmation.vue";
import SnackbarNotification from "@/components/SnackbarNotification.vue";

import { BuildRequest, BuildResponse, FloorRequest } from "@/interfaces";
import { getBuilds, deleteBuild } from "@/api/builds";

import {
  useBuildForm,
  useFloorForm,
  useDeleteConfirmation,
  useSnackbar,
} from "@/composables";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    BuildCard,
    BuildForm,
    FloorForm,
    DeleteConfirmation,
    SnackbarNotification,
  },

  setup() {
    const builds: Ref<BuildResponse[]> = ref([]);
    const buildShortName = ref("");

    const {
      form: buildForm,
      formAction: buildFormAction,
      build,
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

    const {
      deleteConfirmation,
      deleteItemName,
      confirm: deleteConfirm,
      cancel: deleteCancel,
    } = useDeleteConfirmation();

    const {
      snackbar,
      snackbarType,
      text: snackbarText,
      open: openSnackbar,
      close: closeSnackbar,
    } = useSnackbar();

    return {
      builds,
      buildShortName,

      // Build form.
      buildForm,
      buildFormAction,
      build,
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
      deleteConfirm,
      deleteCancel,

      // Snackbar.
      snackbar,
      snackbarType,
      snackbarText,
      openSnackbar,
      closeSnackbar,
    };
  },

  methods: {
    async displayBuilds() {
      this.builds = await getBuilds();
    },

    handleDelete(b: BuildResponse) {
      this.deleteItemName = b.name;
      this.buildShortName = b.shortName;
      this.deleteConfirmation = true;
    },

    async confirmDelete() {
      await this.deleteConfirm(
        () => deleteBuild(this.buildShortName),
        () => {
          this.openSnackbar("success", `${this.deleteItemName} deleted`);
          this.displayBuilds();
        },
        () =>
          this.openSnackbar("error", `Failed to delete ${this.deleteItemName}`)
      );
    },

    async handleSubmitBuild(b: BuildRequest, action: "Add" | "Edit") {
      try {
        await this.submitBuildForm(b, action);
        this.displayBuilds();
        this.openSnackbar(
          "success",
          `${b.name} succesfully ${action.toLowerCase()}ed`
        );
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to ${action.toLowerCase()} build`);
      }
    },

    async handleSubmitFloor(f: FloorRequest) {
      try {
        await this.submitFloorForm(f);
        this.displayBuilds();
        this.openSnackbar("success", `Floor ${f.number} succesfully added`);
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to add floor`);
      }
    },
  },

  created() {
    this.displayBuilds();
  },
});
</script>
