<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        <v-toolbar-title>Switches</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          :append-icon="this.mdiMagnify"
          label="Search"
          color="orange accent-2"
          single-line
          hide-details
        ></v-text-field>
      </v-toolbar>
      <v-data-table
        :headers="headers"
        :items="switches"
        :items-per-page="10"
        :search="search"
        sort-by="name"
        multi-sort
        class="elevation-1"
      >
        <template v-slot:[`item.actions`]="{ item }">
          <v-btn icon small @click="openSwitchForm('Edit', item)">
            <v-icon small>
              {{ mdiPencil }}
            </v-icon>
          </v-btn>
          <v-btn icon small @click="handleDelete(item)">
            <v-icon small>
              {{ mdiDelete }}
            </v-icon>
          </v-btn>
          <v-btn
            icon
            small
            v-if="item.floorNumber"
            :to="{
              name: 'floor',
              params: {
                shortName: item.buildShortName,
                floor: item.floorNumber,
              },
            }"
          >
            <v-icon small>{{ mdiEye }}</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

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
      @click="openSwitchForm('Add')"
    >
      Add switch
    </v-btn>

    <switch-form
      :form="switchForm"
      :action="switchFormAction"
      :needLocationFields="true"
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

    <snackbar
      :snackbar="snackbar"
      :type="snackbarType"
      :text="snackbarText"
      @close="closeSnackbar"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref } from "@vue/composition-api";
import { mdiMagnify, mdiPencil, mdiDelete, mdiEye } from "@mdi/js";

import SwitchForm from "@/components/forms/SwitchForm.vue";
import DeleteConfirmation from "@/components/DeleteConfirmation.vue";
import Snackbar from "@/components/Snackbar.vue";

import { SwitchResponse } from "@/types/switch";
import { getSwitches, deleteSwitch } from "@/api/switches";

import useSwitchForm from "@/composables/useSwitchForm";
import useDeleteConfirmation from "@/composables/useDeleteConfirmation";
import useSnackbar from "@/composables/useSnackbar";

type TableSwitch = SwitchResponse & {
  location: string;
};

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    SwitchForm,
    DeleteConfirmation,
    Snackbar,
  },

  setup() {
    const switches: Ref<TableSwitch[]> = ref([]);

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
      cancel: deleteCancel,
    } = useDeleteConfirmation();

    const {
      snackbar,
      snackbarType,
      text: snackbarText,
      open: openSnackbar,
      close: closeSnackbar,
    } = useSnackbar();

    const search = ref("");
    const headers = ref([
      {
        text: "Name",
        align: "start",
        value: "name",
      },
      { text: "MAC", value: "mac" },
      { text: "IP", value: "ip" },
      { text: "Serial", value: "serial" },
      { text: "Location", value: "location" },
      { text: "Actions", value: "actions", sortable: false },
    ]);

    return {
      switches,

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
      deleteCancel,

      // Snackbar.
      snackbar,
      snackbarType,
      snackbarText,
      openSnackbar,
      closeSnackbar,

      deleteSwitch,

      search,
      headers,

      mdiMagnify,
      mdiPencil,
      mdiDelete,
      mdiEye,
    };
  },

  methods: {
    displaySwitches() {
      getSwitches().then((switches) => {
        this.switches = switches.map((sw) => {
          sw.mac = sw.mac.toUpperCase();
          let tableSwitch = sw as TableSwitch;
          tableSwitch.location =
            (sw.buildShortName ? `${sw.buildShortName}` : "") +
            (sw.floorNumber === null ? "" : `f${sw.floorNumber}`);
          return tableSwitch;
        });
      });
    },

    handleDelete(sw: TableSwitch) {
      this.deleteItemName = sw.name;
      this.deleteConfirmation = true;
    },

    deleteConfirm() {
      deleteSwitch(this.deleteItemName)
        .then(() => {
          this.openSnackbar("success", `${this.deleteItemName} deleted`);
          this.deleteConfirmation = false;
          this.deleteItemName = "";
        })
        .then(() => this.displaySwitches());
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
        await this.submitSwitchForm(
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
        this.displaySwitches();
        this.openSnackbar(
          "success",
          `${name} succesfully ${action.toLowerCase()}ed`
        );
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to ${action.toLowerCase()} switch`);
      }
    },
  },

  created() {
    this.displaySwitches();
  },
});
</script>
