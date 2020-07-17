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
            <v-btn icon :color="hover ? 'orange darken-1' : ''" @click="openSwitchForm('Add')">
              <v-icon dark>{{ mdiPlus }}</v-icon>
            </v-btn>
          </v-hover>
        </v-toolbar>
      </div>

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
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "@vue/composition-api";
import { mdiMagnify, mdiPlus } from "@mdi/js";

import drag from "@/directives/drag";
import zoom from "@/directives/zoom";

import SwitchForm from "@/components/forms/SwitchForm.vue";
import PlanUpload from "@/components/PlanUpload.vue";

import useSwitches from "@/helpers/useSwitches";

export default defineComponent({
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

  setup(props) {
    const planPath = ref(`/plans/${props.build}f${props.floor}.png`);
    const noPlan = ref(false);

    const uploadPlan = () => {
      console.log("ll");
    };

    const switches = ref([
      { name: "b9f1r108", positionTop: "2673.33", positionLeft: "2828.33" }
    ]);

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
      openSwitchForm,
      handleSubmitSwitch,
      closeSwitchForm
    } = useSwitches();

    return {
      planPath,
      noPlan,

      uploadPlan,

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
      handleSubmitSwitch,
      closeSwitchForm,

      mdiMagnify,
      mdiPlus
    };
  },

  created() {
    //this.getSwitchesOf(this.build, this.floor);
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