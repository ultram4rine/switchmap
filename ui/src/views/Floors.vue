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
      :action="action"
      :needLocationFields="false"
      :name="switchName"
      :ipResolveMethod="switchIPResolveMethod"
      :ip="switchIP"
      :mac="switchMAC"
      :snmpCommunity="switchSNMPCommunity"
      :build="switchBuild"
      :floor="switchFloor"
      @submit="addSwitch"
      @close="closeSwitchForm"
    />

    <Snackbar :snackbar="snackbar" :item="item" :action="snackbarAction" @update="updateSnackbar" />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiClose, mdiDelete } from "@mdi/js";
import axios, { AxiosResponse } from "axios";

import { config } from "@/config";
import { Build } from "@/interfaces";

import floorsMixin from "@/mixins/floorsMixin";
import switchesMixin from "@/mixins/switchesMixin";

import FloorForm from "@/components/forms/FloorForm.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";

import Snackbar from "@/components/Snackbar.vue";

export default mixins(floorsMixin, switchesMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true },
    build: { type: String, required: true }
  },

  components: {
    FloorForm,
    SwitchForm,
    Snackbar
  },

  data() {
    return {
      mdiClose: mdiClose,
      mdiDelete: mdiDelete
    };
  },

  created() {
    this.getFloorsOf(this.build);
  },

  methods: {
    handleSubmitFloor(number: string) {
      axios
        .get<Build, AxiosResponse<Build>>(
          `${config.apiURL}/build/${this.build}`
        )
        .then(resp => {
          this.floorBuildName = resp.data.name;
          this.floorNumber = number;
          this.addFloor();
          this.getFloorsOf(this.build);
        })
        .catch(err => console.log(err));
    }
  }
});
</script>