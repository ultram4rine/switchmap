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
        <v-btn color="error" @click="floorForm = !floorForm">Add floor</v-btn>
      </v-card>
    </v-row>

    <v-snackbar v-model="snackbar" :timeout="timeout">
      {{ item }} added!
      <v-btn fab x-small @click="snackbar = false">
        <v-icon dark>{{ mdiClose }}</v-icon>
      </v-btn>
    </v-snackbar>

    <FloorForm :form="floorForm" @submit="addFloor" @close="closeFloorForm" />

    <SwitchForm
      :form="switchForm"
      :action="action"
      :needLocationFields="false"
      @submit="addSwitch"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiClose, mdiDelete } from "@mdi/js";

import floorsMixin from "../mixins/floorsMixin";
import switchesMixin from "../mixins/switchesMixin";

import FloorForm from "./forms/FloorForm.vue";
import SwitchForm from "./forms/SwitchForm.vue";

export default mixins(floorsMixin, switchesMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true },
    build: { type: String, required: true }
  },

  components: {
    FloorForm,
    SwitchForm
  },

  data() {
    return {
      mdiClose: mdiClose,
      mdiDelete: mdiDelete
    };
  },

  created() {
    this.getFloorsOf(this.build);
  }
});
</script>