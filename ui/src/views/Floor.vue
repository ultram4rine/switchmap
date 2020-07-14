<template>
  <div>
    <div id="plan_upload" v-if="noPlan">
      <PlanUpload @upload="uploadPlan" />
    </div>

    <div v-else>
      <div id="floor">
        <div v-drag v-zoom id="plan">
          <v-img :src="planPath" class="image" @error="noPlan = true"></v-img>

          <v-chip
            v-for="sw in switches"
            :key="sw.name"
            dark
            class="switch ma-2"
            :style="{ top: sw.positionTop + 'px', left: sw.positionLeft + 'px' }"
          >{{ sw.name }}</v-chip>
        </div>

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
      </div>

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
        @submit="handleSubmitSwitch"
        @close="closeSwitchForm"
      />
    </div>
  </div>
</template>

<script lang="ts">
import mixins from "vue-typed-mixins";
import { mdiMagnify, mdiPlus } from "@mdi/js";
import switchesMixin from "@/mixins/switchesMixin";
import drag from "@/directives/drag";
import zoom from "@/directives/zoom";
import SwitchForm from "@/components/forms/SwitchForm.vue";
import PlanUpload from "@/components/PlanUpload.vue";
export default mixins(switchesMixin).extend({
  props: {
    isLoading: { type: Boolean, required: true },
    build: { type: String, required: true },
    floor: { type: String, required: true }
  },
  components: {
    SwitchForm,
    PlanUpload
  },
  directives: {
    drag,
    zoom
  },
  data() {
    return {
      mdiMagnify: mdiMagnify,
      mdiPlus: mdiPlus,
      switches: [
        { name: "b9f1r108", positionTop: "2673.33", positionLeft: "2828.33" }
      ],
      planPath: `/plans/${this.build}f${this.floor}.png`,
      noPlan: false
    };
  },
  created() {
    //this.getSwitchesOf(this.build, this.floor);
  },
  methods: {
    uploadPlan() {
      console.log("ll");
    },
    handleSubmitSwitch(
      name: string,
      mac: string,
      snmpCommunity: string,
      ipResolveMethod: string,
      ip: string,
      build: string,
      floor: string
    ) {
      this.switchName = name;
      this.switchMAC = mac;
      this.switchSNMPCommunity = snmpCommunity;
      this.switchIPResolveMethod = ipResolveMethod;
      this.switchIP = ip;
      this.switchBuild = build;
      this.switchFloor = floor;
      this.addSwitch(build, floor);
    }
  }
});
</script>

<style>
#floor {
  position: fixed;
}
#plan {
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
.switch {
  position: absolute;
}
</style>