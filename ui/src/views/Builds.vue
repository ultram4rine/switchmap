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
          @handleAddFloor="handleAddFloor"
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

    <delete-confirmation
      :confirmation="deleteConfirmation"
      :name="deleteItemName"
      @confirm="deleteConfirm"
      @cancel="deleteCancel"
    />

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
      openBuildForm,
      closeBuildForm,
    } = useBuildForm();

    const { deleteConfirmation, deleteItemName } = useDeleteConfirmation();

    const floorForm = ref(false);
    const floor: Ref<FloorRequest> = ref({} as FloorRequest);

    return {
      builds,
      buildShortName,

      // Build form.
      buildForm,
      buildFormAction,
      build,
      oldBuildShortName,
      openBuildForm,
      closeBuildForm,

      // Delete confirmation.
      deleteConfirmation,
      deleteItemName,

      // Floor form.
      floorForm,
      floor,

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

    handleAddFloor(b: BuildResponse) {
      this.build = b;
      this.floorNumber = 0;
      this.floorForm = true;
    },

    handleSubmitBuild(name: string, shortName: string, action: "Add" | "Edit") {
      try {
        switch (action) {
          case "Add":
            addBuild({ name, shortName } as BuildRequest).then(() =>
              this.displayBuilds()
            );
            this.closeBuildForm();
            break;
          case "Edit":
            editBuild(
              { name, shortName } as BuildRequest,
              this.oldBuildShortName
            ).then(() => this.displayBuilds());
            this.closeBuildForm();
            break;
          default:
            break;
        }
      } catch (err: any) {
        console.log(err);
      }
    },

    handleSubmitFloor(number: number) {
      try {
        addFloor({
          number,
          buildName: this.build.name,
          buildShortName: this.build.shortName,
        } as FloorRequest).then(() => this.displayBuilds());
        this.closeFloorForm();
      } catch (error: any) {
        console.log(error);
      }
    },

    closeFloorForm() {
      this.build = {} as BuildRequest;
      this.floorNumber = 0;
      this.floorForm = false;
    },
  },

  created() {
    this.displayBuilds();
  },
});
</script>
