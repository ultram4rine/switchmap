<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        <v-toolbar-title>Switches</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-btn color="orange darken-1" dark @click="openSwitchForm('Add')">
          Add switch
        </v-btn>
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
          <v-btn icon small @click="openSwitchForm('Change', item)">
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

    <delete-confirmation
      :confirmation="deleteConfirmation"
      :name="deleteItemName"
      @confirm="deleteConfirm"
      @cancel="deleteCancel"
    />

    <switch-form
      :form="switchForm"
      :action="switchFormAction"
      :needLocationFields="true"
      :sw="sw"
      @submit="handleSubmitSwitch"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref } from "@vue/composition-api";
import { mdiMagnify, mdiPencil, mdiDelete, mdiEye } from "@mdi/js";

import SwitchForm from "../components/forms/SwitchForm.vue";
import DeleteConfirmation from "../components/DeleteConfirmation.vue";

import { SwitchRequest, SwitchResponse } from "../types/switch";
import {
  getSwitches,
  addSwitch,
  editSwitch,
  deleteSwitch,
} from "../api/switches";

import useDeleteConfirmation from "@/helpers/useDeleteConfirmation";
import { macDenormalization } from "../helpers";

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
  },

  setup() {
    const switches: Ref<TableSwitch[]> = ref([]);

    const switchForm = ref(false);
    const switchFormAction = ref("");
    const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);

    const { deleteConfirmation, deleteItemName } = useDeleteConfirmation();

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

      switchForm,
      switchFormAction,
      sw,

      deleteConfirmation,
      deleteItemName,
      name,

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
          sw.mac = macDenormalization(sw.mac);
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
          this.deleteConfirmation = false;
          this.deleteItemName = "";
        })
        .then(() => this.displaySwitches());
    },

    deleteCancel() {
      this.deleteConfirmation = false;
      this.deleteItemName = "";
    },

    openSwitchForm(action: string, sw?: TableSwitch) {
      if (sw) {
        this.sw = sw as unknown as SwitchRequest;
        this.sw.ipResolveMethod = "Direct";
        this.sw.retrieveFromNetData = false;
        this.sw.retrieveUpLinkFromSeens = false;
        this.sw.retrieveTechDataFromSNMP = false;
      } else {
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
          buildShortName: "",
          floorNumber: 0,
        } as SwitchRequest;
      }
      this.switchFormAction = action;
      this.switchForm = true;
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
      action: string
    ) {
      try {
        switch (action) {
          case "Add": {
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
              buildShortName: build,
              floorNumber: floor,
              revision,
              serial,
            } as SwitchRequest).then(() => this.displaySwitches());
            this.closeSwitchForm();
            break;
          }
          case "Change": {
            editSwitch({
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
              buildShortName: build,
              floorNumber: floor,
              positionTop: this.sw.positionTop,
              positionLeft: this.sw.positionLeft,
              revision,
              serial,
            } as SwitchRequest).then(() => this.displaySwitches());
            this.closeSwitchForm();
            break;
          }
          default:
            break;
        }
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
    this.displaySwitches();
  },
});
</script>
