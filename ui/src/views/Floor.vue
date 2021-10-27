<template>
  <div>
    <div id="plan_upload" v-if="noPlan">
      <plan-upload @upload="uploadPlan" />
    </div>

    <div v-else>
      <div id="floor">
        <div v-drag v-zoom id="plan">
          <v-img :src="planPath" class="image" @error="noPlan = true"></v-img>

          <v-chip
            v-for="sw in switches"
            :key="sw.name"
            :id="sw.name"
            dark
            class="switch ma-2"
            :style="{
              top: sw.positionTop + 'px',
              left: sw.positionLeft + 'px',
            }"
          >
            {{ sw.name }}
          </v-chip>
        </div>

        <v-toolbar dense floating>
          <v-text-field
            hide-details
            color="orange darken-1"
            :prepend-icon="this.mdiMagnify"
            single-line
          ></v-text-field>
          <v-hover v-slot:default="{ hover }">
            <v-btn
              icon
              :color="hover ? 'orange darken-1' : ''"
              @click="openSwitchForm('Add')"
            >
              <v-icon dark>{{ mdiPlus }}</v-icon>
            </v-btn>
          </v-hover>
        </v-toolbar>
      </div>

      <SwitchForm
        :form="switchForm"
        :action="switchFormAction"
        :needLocationFields="false"
        :sw="sw"
        @submit="handleSubmitSwitch"
        @close="closeSwitchForm"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";
import { mdiMagnify, mdiPlus } from "@mdi/js";

import drag from "../directives/drag";
import zoom from "../directives/zoom";

import SwitchForm from "../components/forms/SwitchForm.vue";
import PlanUpload from "../components/PlanUpload.vue";

import { SwitchRequest } from "../types/switch";
import { getSwitches, addSwitch } from "../api/switches";
import { getPlan } from "../api/plans";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
    floor: { type: String, required: true },
  },

  components: {
    SwitchForm,
    PlanUpload,
  },

  directives: {
    drag,
    zoom,
  },

  setup() {
    const planPath = ref("");

    const noPlan = ref(false);

    const uploadPlan = () => {
      console.log("ll");
    };

    const switchForm = ref(false);
    const switchFormAction = ref("");
    const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);

    const switches = ref([
      { name: "b9f1r108", positionTop: "2673.33", positionLeft: "2828.33" },
    ]);

    return {
      planPath,
      noPlan,

      uploadPlan,

      switches,

      switchForm,
      switchFormAction,
      sw,

      mdiMagnify,
      mdiPlus,
    };
  },

  methods: {
    openSwitchForm(action: string) {
      this.sw = {
        retrieveFromNetData: true,
        name: "",
        ip: "",
        mac: "",
        buildShortName: this.shortName,
        floorNumber: this.floor,
      } as unknown as SwitchRequest;
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
    getPlan(`/static/plans/${this.shortName}f${this.floor}.png`)
      .then((uri) => {
        if (uri) {
          this.planPath = uri;
        } else {
          this.noPlan = true;
        }
      })
      .catch((_err: any) => {
        this.noPlan = true;
      });
    //this.getSwitchesOf(this.build, this.floor).then(sws => (this.switches = sws));
  },
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
