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
      ></v-data-table>
    </v-card>

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
import { mdiMagnify } from "@mdi/js";

import SwitchForm from "../components/forms/SwitchForm.vue";

import { SwitchRequest, SwitchResponse } from "../types/switch";
import { getSwitches, addSwitch } from "../api/switches";
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
  },

  setup() {
    const switches: Ref<TableSwitch[]> = ref([]);

    const switchForm = ref(false);
    const switchFormAction = ref("");
    const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);

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
    ]);

    return {
      switches,

      switchForm,
      switchFormAction,
      sw,

      search,
      headers,

      mdiMagnify,
    };
  },

  methods: {
    openSwitchForm(action: string) {
      this.sw = {
        retrieveFromNetData: true,
        name: "",
        ip: "",
        mac: "",
        buildShortName: "",
        floorNumber: 0,
      } as SwitchRequest;
      this.switchFormAction = action;
      this.switchForm = true;
    },

    handleSubmitSwitch(
      name: string,
      ipResolveMethod: string,
      ip: string,
      mac: string,
      snmpCommunity: string,
      build: string,
      floor: number,
      retrieveFromNetData: boolean,
      _action: string
    ) {
      try {
        addSwitch({
          snmpCommunity,
          retrieveFromNetData,
          ipResolveMethod,
          name,
          ip,
          mac,
          buildShortName: build,
          floorNumber: floor,
        } as SwitchRequest);
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
});
</script>
