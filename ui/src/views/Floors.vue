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
          @handleAddSwitch="openSwitchForm('Add', shortName, f.number)"
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
        <v-btn color="error" @click="openFloorForm(shortName)">Add floor</v-btn>
      </v-card>
    </v-row>

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

import FloorCard from "@/components/cards/FloorCard.vue";
import DeleteConfirmation from "@/components/DeleteConfirmation.vue";
import FloorForm from "@/components/forms/FloorForm.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";

import { FloorRequest, FloorResponse } from "@/types/floor";
import { getFloorsOf, deleteFloor } from "@/api/floors";

import useFloorForm from "@/composables/useFloorForm";
import useSwitchForm from "@/composables/useSwitchForm";
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

    const {
      form: floorForm,
      floor,
      buildShortName: floorBuildShortName,
      openForm: openFloorForm,
      submitForm: submitFloorForm,
      closeForm: closeFloorForm,
    } = useFloorForm();

    const {
      form: switchForm,
      formAction: switchFormAction,
      sw,
      oldSwitchName,
      openForm: openSwitchForm,
      submitForm: submitSwitchForm,
      closeForm: closeSwitchForm,
    } = useSwitchForm();

    const { deleteConfirmation, deleteItemName } = useDeleteConfirmation();

    return {
      floors,

      // Floor form.
      floorForm,
      floor,
      floorBuildShortName,
      openFloorForm,
      submitFloorForm,
      closeFloorForm,

      // Switch form.
      switchForm,
      switchFormAction,
      sw,
      oldSwitchName,
      openSwitchForm,
      submitSwitchForm,
      closeSwitchForm,

      // Delete confirmation.
      deleteConfirmation,
      deleteItemName,

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

    handleSubmitFloor(number: number) {
      this.submitFloorForm(number, this.displayFloors);
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
      build: string,
      floor: number,
      retrieveFromNetData: boolean,
      retrieveUpLinkFromSeens: boolean,
      retrieveTechDataFromSNMP: boolean,
      action: "Add" | "Edit"
    ) {
      this.submitSwitchForm(
        name,
        ipResolveMethod,
        ip,
        mac,
        upSwitchName,
        upLink,
        snmpCommunity,
        revision,
        serial,
        build,
        floor,
        retrieveFromNetData,
        retrieveUpLinkFromSeens,
        retrieveTechDataFromSNMP,
        action,
        this.displayFloors
      );
    },
  },

  created() {
    this.displayFloors();
  },
});
</script>
