<template>
  <div>
    <div id="plan_upload" v-if="noPlan">
      <plan-upload
        :update="update"
        @upload="handlePlanUpload"
        @cancel="handleUploadCancel"
      />
    </div>

    <div v-else>
      <div id="floor">
        <div v-drag v-zoom id="plan" :key="planKey">
          <v-img :src="planPath" class="image" @error="noPlan = true"></v-img>

          <switch-item
            v-for="sw in switches"
            :key="sw.name"
            :sw="sw"
            :sendSocket="sendSock"
            :receiveSocket="receiveSock"
            @moving="setMoving"
          >
            {{ sw.name }}
          </switch-item>
        </div>

        <v-toolbar floating>
          <v-icon v-if="wsState === 'loading'" class="ma-2 mdi-spin">
            {{ mdiLoading }}
          </v-icon>
          <v-icon v-else-if="wsState === 'opened'" class="ma-2">
            {{ mdiCheckCircleOutline }}
          </v-icon>
          <v-icon v-else class="ma-2">{{ mdiAlertCircle }}</v-icon>
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
          <v-btn class="ml-2" color="orange darken-1" @click="updatePlan()">
            Update plan
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
        class="white--text"
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

      <snackbar-notification
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
import {
  mdiMagnify,
  mdiPlus,
  mdiLoading,
  mdiCheckCircleOutline,
  mdiAlertCircle,
} from "@mdi/js";

import drag from "@/directives/drag";
import dragSwitch from "@/directives/dragSwitch";
import zoom from "@/directives/zoom";

import PlanUpload from "@/components/PlanUpload.vue";
import SwitchItem from "@/components/SwitchItem.vue";
import SwitchForm from "@/components/forms/SwitchForm.vue";
import SnackbarNotification from "@/components/SnackbarNotification.vue";

import {
  SwitchPosition,
  SwitchRequest,
  SwitchResponse,
} from "@/interfaces/switch";
import {
  getUnplacedSwitchesOfBuild,
  getPlacedSwitchesOfFloor,
} from "@/api/switches";
import { getPlan, uploadPlan } from "@/api/plans";

import { useSwitchForm, useSnackbar, useWebSocket } from "@/composables";

import { authHeader } from "@/helpers";

type SwitchWithMoving = SwitchResponse & { moving: boolean };

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
    floor: { type: String, required: true },
  },

  components: {
    PlanUpload,
    SwitchItem,
    SwitchForm,
    SnackbarNotification,
  },

  directives: {
    drag,
    dragSwitch,
    zoom,
  },

  setup(props) {
    const planPath = ref("");
    const update = ref(false);
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
    const switches: Ref<SwitchWithMoving[]> = ref([]);
    const switchesWithoutPosition: Ref<SwitchWithMoving[]> = ref([]);

    const moving = ref("");
    const locked = ref(new Set<string>());

    document.cookie = "X-Auth-Token=" + authHeader() + "; path=/";

    const { wsState, sendSock, receiveSock } = useWebSocket(
      props.shortName,
      parseInt(props.floor)
    );

    receiveSock.onmessage = (event) => {
      try {
        const pos: SwitchPosition = JSON.parse(event.data);
        //console.log(pos);
        const sw2up = switches.value.find((sw) => sw.name === pos.name);
        if (sw2up && !sw2up.moving) {
          console.log(sw2up.moving);
          const idx = switches.value.indexOf(sw2up);
          switches.value[idx].positionTop = pos.top;
          switches.value[idx].positionLeft = pos.left;
        }
      } catch (e) {
        console.log(event.data);
      }
    };

    return {
      planPath,
      update,
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
      locked,
      // WebSocket.
      wsState,
      sendSock,
      receiveSock,

      mdiMagnify,
      mdiPlus,
      mdiLoading,
      mdiCheckCircleOutline,
      mdiAlertCircle,
    };
  },

  methods: {
    handleUpdatePos(pos: { name: string; posTop: number; posLeft: number }) {
      const sw2up = this.switches.find((sw) => {
        return sw.name === pos.name;
      });
      if (sw2up) {
        const idx = this.switches.indexOf(sw2up);
        this.switches[idx].positionTop = pos.posTop;
        this.switches[idx].positionLeft = pos.posLeft;
      }
    },

    async displaySwitches() {
      const switches = await getPlacedSwitchesOfFloor(
        this.shortName,
        parseInt(this.floor)
      );
      this.switches = switches.map((sw) => sw as SwitchWithMoving);

      const switchesWithoutPosition = await getUnplacedSwitchesOfBuild(
        this.shortName
      );
      this.switchesWithoutPosition = switchesWithoutPosition.map(
        (sw) => sw as SwitchWithMoving
      );

      this.switches = this.switches.concat(this.switchesWithoutPosition);

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

    updatePlan() {
      this.update = true;
      this.noPlan = true;
    },

    handlePlanUpload(plan: File) {
      uploadPlan(this.shortName, parseInt(this.floor), plan).then(() =>
        this.showPlan()
      );
    },

    handleUploadCancel() {
      this.update = false;
      this.noPlan = false;
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

    setMoving(a: { name: string; val: boolean }) {
      const sw2up = this.switches.find((sw) => {
        return sw.name === a.name;
      });
      if (sw2up) {
        const idx = this.switches.indexOf(sw2up);
        this.switches[idx].moving = a.val;
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

  beforeDestroy() {
    document.cookie = "X-Auth-Token=; path=/";
    this.sendSock.close(1000);
    this.receiveSock.close(1000);
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
.mdi-spin {
  animation-name: spin;
  animation-duration: 1s;
  animation-iteration-count: infinite;
  animation-timing-function: linear;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
