<template>
  <div id="switches">
    <v-card>
      <v-toolbar dark flat>
        <v-toolbar-title>Switches</v-toolbar-title>
        <v-divider class="mx-4" inset vertical></v-divider>
        <v-btn color="orange darken-1" dark @click="switchForm = !switchForm">Add switch</v-btn>
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
      :action="action"
      :needLocationFields="true"
      :name="switchName"
      :ipResolveMethod="switchIPResolveMethod"
      :ip="switchIP"
      :mac="switchMAC"
      :snmpCommunity="switchSNMPCommunity"
      :build="switchBuild"
      :floor="switchFloor"
      @submit="addSwitch('', '')"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiMagnify } from "@mdi/js";

import switchesMixin from "@/mixins/switchesMixin";

import SwitchForm from "@/components/forms/SwitchForm.vue";

export default mixins(switchesMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  components: {
    SwitchForm
  },

  data() {
    return {
      mdiMagnify: mdiMagnify,

      search: "",

      headers: [
        {
          text: "Name",
          align: "start",
          value: "name"
        },
        { text: "MAC", value: "mac" },
        { text: "IP", value: "ip" },
        { text: "Serial", value: "serial" },
        { text: "Location", value: "location" }
      ]
    };
  },

  created() {
    this.getAllSwitches();
  }
});
</script>