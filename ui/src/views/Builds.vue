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
          @handleEdit="handleEdit"
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

    <confirmation
      :confirmation="confirmation"
      :name="name"
      @confirm="
        deleteBuild(shortName),
          (confirmation = !confirmation),
          (name = ''),
          (shortName = '')
      "
      @cancel="(confirmation = !confirmation), (name = ''), (shortName = '')"
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
import Confirmation from "../components/Confirmation.vue";
import BuildForm from "../components/forms/BuildForm.vue";
import FloorForm from "../components/forms/FloorForm.vue";

import { BuildRequest, BuildResponse } from "../types/build";
import { FloorRequest } from "../types/floor";
import { getBuilds, addBuild, editBuild, deleteBuild } from "../api/builds";
import { addFloor } from "../api/floors";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    BuildCard,
    Confirmation,
    BuildForm,
    FloorForm,
  },

  setup() {
    const builds: Ref<BuildResponse[]> = ref([]);

    const confirmation = ref(false);
    const name = ref("");
    const shortName = ref("");

    const buildForm = ref(false);
    const buildFormAction = ref("");
    const build: Ref<BuildRequest> = ref({} as BuildRequest);
    const oldBuild = ref("");

    const floorForm = ref(false);
    const floor: Ref<FloorRequest> = ref({} as FloorRequest);

    return {
      builds,

      confirmation,
      name,
      shortName,

      buildForm,
      buildFormAction,
      build,
      oldBuild,

      floorForm,
      floor,

      deleteBuild,
    };
  },

  methods: {
    displayBuilds() {
      getBuilds().then((builds) => (this.builds = builds));
    },

    handleEdit(b: BuildResponse) {
      this.build = b;
      this.oldBuild = b.shortName;
      this.buildFormAction = "Change";
      this.buildForm = true;
    },

    handleDelete(b: BuildResponse) {
      this.name = b.name;
      this.shortName = b.shortName;
      this.confirmation = true;
    },

    handleAddFloor(b: BuildResponse) {
      this.build = b;
      this.floorNumber = 0;
      this.floorForm = true;
    },

    openBuildForm(action: string) {
      this.build = {} as BuildRequest;
      this.buildFormAction = action;
      this.buildForm = true;
    },

    handleSubmitBuild(name: string, shortName: string, action: string) {
      try {
        switch (action) {
          case "Add":
            addBuild({ name, shortName } as BuildRequest).then(() =>
              this.displayBuilds()
            );
            this.closeBuildForm();
            break;
          case "Change":
            editBuild({ name, shortName } as BuildRequest, this.oldBuild).then(
              () => this.displayBuilds()
            );
            this.closeBuildForm();
            break;
          default:
            break;
        }
      } catch (err: any) {
        console.log(err);
      }
    },

    closeBuildForm() {
      this.build = {} as BuildRequest;
      this.oldBuild = "";
      this.buildFormAction = "";
      this.buildForm = false;
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
