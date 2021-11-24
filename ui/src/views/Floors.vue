<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col
        v-if="isLoading && floors.length === 0"
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
          @handleAddSwitch="
            openSwitchForm('Add', undefined, shortName, f.number)
          "
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
      dark
      @click="openFloorForm(shortName)"
    >
      Add floor
    </v-btn>

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
      @confirm="confirmDelete"
      @cancel="deleteCancel(() => (floor = {}))"
    />

    <snackbar
      :snackbar="snackbar"
      :type="snackbarType"
      :text="snackbarText"
      @close="closeSnackbar"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";

import FloorCard from "@/components/cards/FloorCard.vue";
import FloorForm from "@/components/forms/FloorForm.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";
import DeleteConfirmation from "@/components/DeleteConfirmation.vue";
import Snackbar from "@/components/Snackbar.vue";

import { FloorRequest, FloorResponse } from "@/interfaces/floor";
import { getFloorsOf, deleteFloor } from "@/api/floors";

import {
  useFloorForm,
  useSwitchForm,
  useDeleteConfirmation,
  useSnackbar,
} from "@/composables";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
  },

  components: {
    FloorCard,
    FloorForm,
    SwitchForm,
    DeleteConfirmation,
    Snackbar,
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
      openForm: openSwitchForm,
      submitForm: submitSwitchForm,
      closeForm: closeSwitchForm,
    } = useSwitchForm();

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
      openSwitchForm,
      submitSwitchForm,
      closeSwitchForm,

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
    async displayFloors() {
      this.floors = await getFloorsOf(this.shortName);
    },

    handleDelete(floor: FloorResponse) {
      this.deleteItemName = `Floor ${floor.number}`;
      this.floor = { number: floor.number } as FloorRequest;
      this.deleteConfirmation = true;
    },

    async confirmDelete() {
      await this.deleteConfirm(
        () => deleteFloor(this.shortName, this.floor.number),
        () => {
          this.openSnackbar("success", `${this.deleteItemName} deleted`);
          this.displayFloors();
        },
        () =>
          this.openSnackbar("error", `Failed to delete ${this.deleteItemName}`)
      );
    },

    async handleSubmitFloor(number: number) {
      try {
        await this.submitFloorForm(number);
        this.displayFloors();
        this.openSnackbar("success", `Floor ${number} succesfully added`);
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to add floor`);
      }
    },

    async handleSubmitSwitch(
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
      try {
        const sr = await this.submitSwitchForm(
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
          action
        );
        this.displayFloors();

        let typ: "success" | "info" | "warning" | "error" = "success";
        let text = "";

        if (sr.seen && sr.snmp) {
          typ = "success";
          text = `${name} succesfully ${action.toLowerCase()}ed`;
        } else if (!sr.seen && !sr.snmp) {
          typ = "warning";
          text = `Failed to get uplink and technical data of ${name}`;
        } else if (!sr.seen) {
          typ = "warning";
          text = `Failed to get uplink of ${name}`;
        } else if (!sr.snmp) {
          typ = "warning";
          text = `Failed to get technical data of ${name}`;
        }

        this.openSnackbar(typ, text);
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to ${action.toLowerCase()} switch`);
      }
    },
  },

  created() {
    this.displayFloors();
  },
});
</script>
