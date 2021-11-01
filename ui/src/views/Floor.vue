<template>
  <div>
    <div id="plan_upload" v-if="noPlan">
      <plan-upload @upload="handlePlanUpload" />
    </div>

    <div v-else>
      <div id="floor">
        <div v-drag v-zoom id="plan" :key="planKey">
          <v-img :src="planPath" class="image" @error="noPlan = true"></v-img>

          <v-chip
            v-for="sw in switches"
            v-drag-switch="sw"
            :key="sw.name"
            :id="sw.name"
            dark
            class="switch ma-2"
            :style="
              sw.positionTop && sw.positionLeft
                ? {
                    top: sw.positionTop + 'px',
                    left: sw.positionLeft + 'px',
                  }
                : { display: 'none' }
            "
          >
            {{ sw.name }}
          </v-chip>
        </div>

        <v-toolbar floating>
          <v-select
            v-model="swName"
            :items="switchesWithoutPosition"
            hide-details
            item-text="name"
            item-value="name"
            label="Switch"
            color="orange accent-2"
          ></v-select>
          <v-hover v-slot:default="{ hover }">
            <v-btn
              icon
              :color="hover ? 'orange darken-1' : ''"
              @click="openSwitchForm('Add')"
            >
              <v-icon dark>{{ mdiPlus }}</v-icon>
            </v-btn>
          </v-hover>
          <v-btn
            v-if="swName !== ''"
            color="orange darken-1"
            @click="place(swName)"
          >
            Place
          </v-btn>
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
import dragSwitch from "../directives/dragSwitch";
import zoom from "../directives/zoom";

import SwitchForm from "../components/forms/SwitchForm.vue";
import PlanUpload from "../components/PlanUpload.vue";

import {
  SwitchRequest,
  SwitchResponse,
  SavePositionRequest,
} from "../types/switch";
import { getSwitchesOfFloor, addSwitch } from "../api/switches";
import { getPlan, uploadPlan } from "../api/plans";

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
    dragSwitch,
    zoom,
  },

  setup() {
    const planPath = ref("");

    const noPlan = ref(false);

    const planKey = ref(0);

    const switchForm = ref(false);
    const switchFormAction = ref("");
    const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);

    const swName = ref("");
    const switches: Ref<SwitchResponse[]> = ref([]);
    const switchesWithoutPosition: Ref<SwitchResponse[]> = ref([]);

    return {
      planPath,
      noPlan,
      planKey,

      uploadPlan,

      swName,
      switches,
      switchesWithoutPosition,

      switchForm,
      switchFormAction,
      sw,

      mdiMagnify,
      mdiPlus,
    };
  },

  methods: {
    handlePlanUpload(plan: File) {
      console.log(plan);
      uploadPlan(this.shortName, this.floor, plan);
    },

    place(name: string) {
      const switchToPlace = this.switchesWithoutPosition.find((sw) => {
        return sw.name == name;
      });

      if (switchToPlace) {
        this.switchesWithoutPosition = this.switchesWithoutPosition.filter(
          (sw) => sw.name !== name
        );

        this.swName = "";

        const plan = document.getElementById("plan");
        if (plan) {
          switchToPlace.positionTop = plan.offsetHeight / 2;
          switchToPlace.positionLeft = plan.offsetWidth / 2;
        }
      }
    },

    openSwitchForm(action: string) {
      this.sw = {
        retrieveFromNetData: true,
        retrieveUpLinkFromSeens: true,
        retrieveTechDataFromSNMP: true,
        name: "",
        ip: "",
        mac: "",
        upSwitchName: "",
        upLink: "",
        revision: "",
        serial: "",
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
      upSwitchName: string,
      upLink: string,
      snmpCommunity: string,
      revision: string,
      serial: string,
      build: string,
      floor: number,
      retrieveFromNetData: boolean,
      retrieveUpLinkFromSeens: boolean,
      retrieveTechDataFromSNMP: boolean,
      _action: string
    ) {
      try {
        addSwitch({
          snmpCommunity,
          retrieveFromNetData,
          retrieveUpLinkFromSeens,
          retrieveTechDataFromSNMP,
          ipResolveMethod,
          name,
          ip,
          mac,
          upSwitchName,
          upLink,
          buildShortName: build,
          floorNumber: floor,
          revision,
          serial,
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
    getSwitchesOfFloor(this.shortName, parseInt(this.floor)).then((sws) => {
      sws.forEach((sw) => {
        this.switches.push(sw);
        if (!sw.positionTop && !sw.positionLeft) {
          this.switchesWithoutPosition.push(sw);
        }
      });
      this.planKey += 1;
    });
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
