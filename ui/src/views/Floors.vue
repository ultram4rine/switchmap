<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader
          class="mx-auto"
          type="card-heading, list-item, actions"
        ></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="f in floors"
        :key="f.number"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <floor-card
          :shortName="shortName"
          :floor="f"
          @handleDelete="handleDelete"
          @handleAddSwitch="handleAddSwitch"
        />
      </v-col>

      <v-col
        v-if="!isLoading && floors.length === 0"
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
                No floors to show
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-card class="ma-1">
        <v-btn color="error" @click="openFloorForm">Add floor</v-btn>
      </v-card>
    </v-row>

    <delete-confirmation
      :confirmation="deleteConfirmation"
      :name="deleteItemName"
      @confirm="deleteConfirm"
      @cancel="deleteCancel"
    />

    <floor-form
      :form="floorForm"
      :floor="floor"
      @submit="handleSubmitFloor"
      @close="closeFloorForm"
    />

    <switch-form
      :form="switchForm"
      :action="switchFormAction"
      :needLocationFields="false"
      :sw="sw"
      @submit="handleSubmitSwitch"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";

import FloorCard from "../components/cards/FloorCard.vue";
import DeleteConfirmation from "../components/DeleteConfirmation.vue";
import FloorForm from "../components/forms/FloorForm.vue";
import SwitchForm from "../components/forms/SwitchForm.vue";

import { FloorRequest, FloorResponse } from "../types/floor";
import { SwitchRequest } from "../types/switch";
import { getFloorsOf, addFloor, deleteFloor } from "../api/floors";
import { getBuild } from "../api/builds";
import { addSwitch } from "../api/switches";

import useDeleteConfirmation from "@/composables/useDeleteConfirmation";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
  },

  components: {
    FloorCard,
    DeleteConfirmation,
    FloorForm,
    SwitchForm,
  },

  setup() {
    const floors: Ref<FloorResponse[]> = ref([]);

    const { deleteConfirmation, deleteItemName } = useDeleteConfirmation();

    const floorForm = ref(false);
    const floor: Ref<FloorRequest> = ref({} as FloorRequest);

    const switchForm = ref(false);
    const switchFormAction = ref("");
    const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);

    return {
      floors,

      deleteConfirmation,
      deleteItemName,

      floorForm,
      floor,

      switchForm,
      switchFormAction,
      sw,

      deleteFloor,
    };
  },

  methods: {
    displayFloors() {
      getFloorsOf(this.shortName).then((floors) => (this.floors = floors));
    },

    handleDelete(floor: FloorResponse) {
      this.deleteItemName = `Floor ${floor.number}`;
      this.floor = { number: floor.number } as FloorRequest;
      this.deleteConfirmation = true;
    },

    deleteConfirm() {
      deleteFloor(this.shortName, this.floor.number)
        .then(() => {
          this.deleteConfirmation = false;
          this.deleteItemName = "";
          this.floor = {} as FloorRequest;
        })
        .then(() => this.displayFloors());
    },

    deleteCancel() {
      this.deleteConfirmation = false;
      this.deleteItemName = "";
      this.floor = {} as FloorRequest;
    },

    handleAddSwitch(_shortName: string, floor: FloorResponse) {
      this.sw = {
        retrieveFromNetData: true,
        retrieveUpLinkFromSeens: true,
        retrieveTechDataFromSNMP: true,
        name: "",
        ip: "",
        mac: "",
        upSwitchName: "",
        upLink: "",
        revision: "",
        serial: "",
        floorNumber: floor.number,
      } as SwitchRequest;
      this.switchFormAction = "Add";
      this.switchForm = true;
    },

    openFloorForm() {
      this.floorForm = true;
      this.floor = {} as FloorRequest;
    },

    handleSubmitFloor(number: number) {
      try {
        getBuild(this.shortName).then((b) => {
          addFloor({
            number,
            buildName: b.name,
            buildShortName: this.shortName,
          } as FloorRequest).then(() => this.displayFloors());
          this.closeFloorForm();
        });
      } catch (error: any) {
        console.log(error);
      }
    },

    closeFloorForm() {
      this.floor = {} as FloorRequest;
      this.floorForm = false;
    },

    handleSubmitSwitch(
      name: string,
      ipResolveMethod: string,
      ip: string,
      mac: string,
      upSwitchName: string,
      upLink: string,
      snmpCommunity: string,
      revision: string,
      serial: string,
      _build: string,
      floor: number,
      retrieveFromNetData: boolean,
      retrieveUpLinkFromSeens: boolean,
      retrieveTechDataFromSNMP: boolean,
      _action: string
    ) {
      try {
        addSwitch({
          snmpCommunity,
          retrieveFromNetData,
          retrieveUpLinkFromSeens,
          retrieveTechDataFromSNMP,
          ipResolveMethod,
          name,
          ip,
          mac,
          upSwitchName,
          upLink,
          buildShortName: this.shortName,
          floorNumber: floor,
          revision,
          serial,
        } as SwitchRequest).then(() => this.displayFloors());
        this.closeSwitchForm();
        this.switchForm = false;
      } catch (error: any) {
        console.log(error);
      }
    },

    closeSwitchForm() {
      this.sw = {} as SwitchRequest;
      this.switchFormAction = "";
      this.switchForm = false;
    },
  },

  created() {
    this.displayFloors();
  },
});
</script>
