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
          <v-btn
            v-if="swName !== ''"
            class="ma-1"
            color="orange darken-1"
            @click="place(swName)"
          >
            Place
          </v-btn>
        </v-toolbar>
      </div>

      <v-btn
        rounded
        large
        fixed
        bottom
        right
        color="orange accent-4"
        dark
        @click="openSwitchForm('Add', undefined, shortName, parseInt(floor))"
      >
        Add switch
      </v-btn>

      <SwitchForm
        :form="switchForm"
        :action="switchFormAction"
        :needLocationFields="false"
        :swit="sw"
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

import { SwitchRequest, SwitchResponse } from "@/interfaces/switch";
import { getSwitchesOfFloor } from "@/api/switches";
import { getPlan, uploadPlan } from "@/api/plans";

import { useSwitchForm, useSnackbar } from "@/composables";

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

  setup() {
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

      mdiMagnify,
      mdiPlus,
    };
  },

  methods: {
    async displaySwitches() {
      const sws = await getSwitchesOfFloor(
        this.shortName,
        parseInt(this.floor)
      );
      sws.forEach((sw) => {
        this.switches.push(sw);
        if (!sw.positionTop && !sw.positionLeft) {
          this.switchesWithoutPosition.push(sw);
        }
      });
      this.planKey += 1;
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

    async handleSubmitSwitch(swit: SwitchRequest, action: "Add" | "Edit") {
      try {
        const sr = await this.submitSwitchForm(swit, action);
        this.displaySwitches();

        let typ: "success" | "info" | "warning" | "error" = "success";
        let text = "";

        if (sr.seen && sr.snmp) {
          typ = "success";
          text = `${sr.sw.name} succesfully ${action.toLowerCase()}ed`;
        } else if (!sr.seen && !sr.snmp) {
          typ = "warning";
          text = `Failed to get uplink and technical data of ${sr.sw.name}`;
        } else if (!sr.seen) {
          typ = "warning";
          text = `Failed to get uplink of ${sr.sw.name}`;
        } else if (!sr.snmp) {
          typ = "warning";
          text = `Failed to get technical data of ${sr.sw.name}`;
        }

        this.openSnackbar(typ, text);
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
