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

    <SwitchForm
      :form="switchForm"
      :action="switchAction"
      :needLocationFields="true"
      :name="switchName"
      :ipResolveMethod="switchIPResolveMethod"
      :ip="switchIP"
      :mac="switchMAC"
      :snmpCommunity="switchSNMPCommunity"
      :build="switchBuild"
      :floor="switchFloor"
      @submit="handleSubmitSwitchFromSwitchesView"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "@vue/composition-api";
import { mdiMagnify } from "@mdi/js";

import SwitchForm from "@/components/forms/SwitchForm.vue";

import useSwitches from "@/helpers/useSwitches";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  components: {
    SwitchForm,
  },

  setup() {
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

    const {
      switches,
      switchForm,
      switchName,
      switchIPResolveMethod,
      switchIP,
      switchMAC,
      switchSNMPCommunity,
      switchBuild,
      switchFloor,
      switchAction,
      openSwitchForm,
      handleSubmitSwitchFromSwitchesView,
      closeSwitchForm,
      getAllSwitches,
    } = useSwitches();

    return {
      search,
      headers,

      switches,

      switchForm,
      switchName,
      switchIPResolveMethod,
      switchIP,
      switchMAC,
      switchSNMPCommunity,
      switchBuild,
      switchFloor,

      switchAction,

      openSwitchForm,
      handleSubmitSwitchFromSwitchesView,
      closeSwitchForm,

      getAllSwitches,

      mdiMagnify,
    };
  },

  created() {
    this.getAllSwitches().then(sws => (this.switches = sws));
  },
});
</script>
