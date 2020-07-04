<template>
  <div>
    <v-toolbar dense floating>
      <v-text-field
        hide-details
        color="orange darken-1"
        :prepend-icon="this.mdiMagnify"
        single-line
      ></v-text-field>
      <v-hover v-slot:default="{ hover }">
        <v-btn icon :color="hover ? 'orange darken-1' : ''" @click="switchForm = !switchForm">
          <v-icon dark>{{ mdiPlus }}</v-icon>
        </v-btn>
      </v-hover>
    </v-toolbar>
    <div id="floor">
      <div v-dragAndScroll class="plan">
        <img :src="planPath" class="image" />
      </div>
    </div>

    <SwitchForm
      v-model="switchForm"
      :action="action"
      @submit="addSwitch"
      @close="closeSwitchForm"
      v-on:emitSwitchName="updateSwitchName"
      v-on:emitSwitchIPResolveMethod="updateSwitchIPResolveMethod"
      v-on:emitSwitchIP="updateSwitchIP"
      v-on:emitSwitchMAC="updateSwitchMAC"
      v-on:emitSwitchSNMPCommunityType="updateSwitchSNMPCommunityType"
      v-on:emitSwitchSNMPCommunity="updateSwitchSNMPCommunity"
    />
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiMagnify, mdiPlus } from "@mdi/js";

import switchesMixin from "../mixins/switchesMixin";

import dragAndScroll from "../directives/dragAndScrollDirective";

import SwitchForm from "./SwitchForm.vue";

export default mixins(switchesMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true },
    build: { type: String, required: true },
    floor: { type: String, required: true }
  },

  components: {
    SwitchForm
  },

  directives: {
    dragAndScroll
  },

  data() {
    return {
      mdiMagnify: mdiMagnify,
      mdiPlus: mdiPlus,

      planPath: `/plans/${this.build}f${this.floor}.png`
    };
  },

  created() {
    this.getAllSwitches();
  }
});
</script>

<style>
#floor {
  position: fixed;
}
.plan {
  width: auto;
  height: auto;
  display: inline-block;
  position: absolute;
  border: 3px solid black;
  transform: scale(0.2);
  -webkit-transform-origin: 0 0;
  -moz-transform-origin: 0 0;
  transform-origin: 0 0;
}
.image {
  position: relative;
}
</style>