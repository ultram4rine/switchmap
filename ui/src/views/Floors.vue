<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader class="mx-auto" type="card-heading, list-item, actions"></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="floor in floors"
        :key="floor.number"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <v-card class="ma-1" outlined>
          <v-card-title class="headline">
            Floor {{ floor.number }}
            <v-spacer></v-spacer>
            <v-btn icon small color="red" @click="deleteFloorOf(build, floor.number)">
              <v-icon>{{ mdiDelete }}</v-icon>
            </v-btn>
          </v-card-title>

          <v-card-subtitle>{{ floor.switchesNumber }} switches</v-card-subtitle>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="switchForm = !switchForm; switchFloorNumber = floor.number"
            >Add switch</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'floor', params: { build: build, floor: floor.number.toString() }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && floors.length===0" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-card class="ma-1" outlined>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">No floors to show</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-card class="ma-1">
        <v-btn color="error" @click="floorBuildShortName = build; floorForm = !floorForm">Add floor</v-btn>
      </v-card>
    </v-row>

    <FloorForm
      :form="floorForm"
      :number="floorNumber"
      @submit="handleSubmitFloor"
      @close="closeFloorForm"
    />

    <SwitchForm
      :form="switchForm"
      :action="switchAction"
      :needLocationFields="false"
      :name="switchName"
      :ipResolveMethod="switchIPResolveMethod"
      :ip="switchIP"
      :mac="switchMAC"
      :snmpCommunity="switchSNMPCommunity"
      :build="switchBuild"
      :floor="switchFloor"
      @submit="handleSubmitSwitch"
      @close="closeSwitchForm"
    />

    <Snackbar :snackbar="snackbar" :item="item" :action="snackbarAction" @update="updateSnackbar" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "@vue/composition-api";
import { mdiDelete } from "@mdi/js";

import FloorForm from "@/components/forms/FloorForm.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";

import Snackbar from "@/components/Snackbar.vue";

import useFloors from "@/helpers/useFloors";
import useSwitches from "@/helpers/useSwitches";
import useConfirmation from "@/helpers/useConfirmation";
import useSnackbar from "@/helpers/useSnackbar";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    build: { type: String, required: true }
  },

  components: {
    FloorForm,
    SwitchForm,
    Snackbar
  },

  setup() {
    const {
      floors,
      floorForm,
      floorNumber,
      floorError,
      getFloorsOf,
      addFloorTo,
      deleteFloorOf
    } = useFloors();

    const {
      switchForm,
      switchName,
      switchIPResolveMethod,
      switchIP,
      switchMAC,
      switchSNMPCommunity,
      switchBuild,
      switchFloor,
      switchAction,
      closeSwitchForm,
      switchError,
      addSwitch
    } = useSwitches();

    const { snackbar, action, item } = useSnackbar();

    return {
      floors,

      floorForm,
      floorNumber,

      floorError,

      getFloorsOf,
      addFloorTo,
      deleteFloorOf,

      switchForm,
      switchName,
      switchIPResolveMethod,
      switchIP,
      switchMAC,
      switchSNMPCommunity,
      switchBuild,
      switchFloor,

      switchAction,

      closeSwitchForm,

      switchError,

      addSwitch,

      snackbar,
      action,
      item,

      mdiDelete
    };
  },

  created() {
    this.getFloorsOf(this.build).then(floors => (this.floors = floors));
  }
});
</script>