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
            v-drag-switch="{ sw, socket }"
            @moving="setMoving"
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
              @click="
                openSwitchForm('Add', undefined, shortName, parseInt(floor))
              "
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

      <snackbar
        :snackbar="snackbar"
        :type="snackbarType"
        :text="snackbarText"
        @close="closeSnackbar"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";
import { mdiMagnify, mdiPlus } from "@mdi/js";

import drag from "@/directives/drag";
import dragSwitch from "@/directives/dragSwitch";
import zoom from "@/directives/zoom";

import PlanUpload from "@/components/PlanUpload.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";
import Snackbar from "@/components/Snackbar.vue";

import { SwitchResponse } from "@/types/switch";
import { getSwitchesOfFloor } from "@/api/switches";
import { getPlan, uploadPlan } from "@/api/plans";
import { wsURL } from "@/api";

import useSwitchForm from "@/composables/useSwitchForm";
import useSnackbar from "@/composables/useSnackbar";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
    floor: { type: String, required: true },
  },

  components: {
    PlanUpload,
    SwitchForm,
    Snackbar,
  },

  directives: {
    drag,
    dragSwitch,
    zoom,
  },

  setup(props) {
    const planPath = ref("");

    const noPlan = ref(false);

    const planKey = ref(0);

    const {
      form: switchForm,
      formAction: switchFormAction,
      sw,
      openForm: openSwitchForm,
      submitForm: submitSwitchForm,
      closeForm: closeSwitchForm,
    } = useSwitchForm();

    const {
      snackbar,
      snackbarType,
      text: snackbarText,
      open: openSnackbar,
      close: closeSnackbar,
    } = useSnackbar();

    const swName = ref("");
    const switches: Ref<SwitchResponse[]> = ref([]);
    const switchesWithoutPosition: Ref<SwitchResponse[]> = ref([]);

    const moving = ref("");
    let socket = new WebSocket(wsURL(props.shortName, parseInt(props.floor)));

    socket.onopen = () => {
      console.log("opened");
    };
    socket.onerror = () => {
      console.log("error");
    };
    socket.onclose = () => {
      console.log("closing");
    };
    socket.onmessage = (event) => {
      const pos: { name: string; top: number; left: number } = JSON.parse(
        event.data
      );
      if (pos) {
        if (moving.value !== pos.name) {
          const sw2Update = switches.value.find((sw) => sw.name === pos.name);
          if (sw2Update) {
            const idx = switches.value.indexOf(sw2Update);
            switches.value[idx].positionTop = pos.top;
            switches.value[idx].positionLeft = pos.left;
          }
        }
      }
    };

    return {
      planPath,
      noPlan,
      planKey,

      uploadPlan,

      swName,
      switches,
      switchesWithoutPosition,

      // Switch form.
      switchForm,
      switchFormAction,
      sw,
      openSwitchForm,
      submitSwitchForm,
      closeSwitchForm,

      // Snackbar.
      snackbar,
      snackbarType,
      snackbarText,
      openSnackbar,
      closeSnackbar,

      moving,

      socket,

      mdiMagnify,
      mdiPlus,
    };
  },

  methods: {
    displaySwitches() {
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

    showPlan() {
      getPlan(`/plans/${this.shortName}f${this.floor}.png`)
        .then((uri) => {
          if (uri) {
            this.planPath = uri;
            this.noPlan = false;
          } else {
            this.noPlan = true;
          }
        })
        .catch(() => {
          this.noPlan = true;
        });
    },

    handlePlanUpload(plan: File) {
      uploadPlan(this.shortName, parseInt(this.floor), plan).then(() =>
        this.showPlan()
      );
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

    setMoving(name: { detail: string }) {
      this.moving = name.detail;
    },

    async handleSubmitSwitch(
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
      action: "Add" | "Edit"
    ) {
      try {
        await this.submitSwitchForm(
          name,
          ipResolveMethod,
          ip,
          mac,
          upSwitchName,
          upLink,
          snmpCommunity,
          revision,
          serial,
          build,
          floor,
          retrieveFromNetData,
          retrieveUpLinkFromSeens,
          retrieveTechDataFromSNMP,
          action
        );
        this.displaySwitches();
        this.openSnackbar(
          "success",
          `${name} succesfully ${action.toLowerCase()}ed`
        );
      } catch (err: unknown) {
        this.openSnackbar("error", `Failed to ${action.toLowerCase()} switch`);
      }
    },
  },

  created() {
    this.showPlan();
    this.displaySwitches();
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
