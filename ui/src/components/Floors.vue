<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" :sm="12" :md="4" :lg="2">
        <v-skeleton-loader type="article" class="mx-auto"></v-skeleton-loader>
      </v-col>
      <v-col v-else v-for="floor in floors" :key="floor.number" :sm="12" :md="4" :lg="2">
        <v-card class="ma-1" outlined>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">Floor {{ floor.number }}</v-list-item-title>
              <v-list-item-subtitle>{{ floor.switchesNumber }} switches</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              dark
              small
              color="primary"
              @click="addSwitchForm = !addSwitchForm; switchFloorNumber = floor.number"
            >Add switch</v-btn>
            <v-btn
              dark
              small
              color="primary"
              :to="{ name: 'floor', params: { addr: $route.params.addr, floor: floor.number }}"
            >Go</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col v-if="!isLoading && floors.length===0" :sm="12" :md="4" :lg="2">
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

    <FloorForm
      v-model="floorForm"
      @submit="addFloor"
      @close="closeFloorForm"
      v-on:emitFloorNumber="updateFloorNumber"
    />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiClose } from "@mdi/js";

import floorsMixin from "../mixins/floorsMixin";

import FloorForm from "./FloorForm.vue";

export default mixins(floorsMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  components: {
    FloorForm
  },

  data() {
    return {
      mdiClose: mdiClose
    };
  },

  created() {
    this.getAllFloors();
  }
});
</script>