<template>
  <div id="build">
    <v-row no-gutters dense>
      <v-col v-if="isLoading" cols="12" sm="6" md="4" lg="3" xl="2">
        <v-skeleton-loader
          class="mx-auto"
          type="card-heading, list-item, actions"
        ></v-skeleton-loader>
      </v-col>
      <v-col
        v-else
        v-for="f in floors"
        :key="f.number"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <floor-card
          :shortName="shortName"
          :floor="f"
          @handleDelete="handleDelete"
          @handleAddSwitch="handleAddSwitch"
        />
      </v-col>

      <v-col
        v-if="!isLoading && floors.length === 0"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="2"
      >
        <v-card class="ma-1" outlined>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline mb-1">
                No floors to show
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-card class="ma-1">
        <v-btn color="error" @click="openFloorForm">Add floor</v-btn>
      </v-card>
    </v-row>

    <confirmation
      :confirmation="confirmation"
      :name="deleteName"
      @confirm="
        deleteFloor(shortName, floorNumber),
          (confirmation = !confirmation),
          (deleteName = ''),
          (floorNumber = 0)
      "
      @cancel="
        (confirmation = !confirmation), (deleteName = ''), (floorNumber = 0)
      "
    />

    <floor-form
      :form="floorForm"
      :floor="floor"
      @submit="handleSubmitFloor"
      @close="closeFloorForm"
    />

    <switch-form
      :form="switchForm"
      :action="switchFormAction"
      :needLocationFields="false"
      :retrieveFromNetdata="true"
      :name="switchName"
      :ipResolveMethod="switchIPResolveMethod"
      :ip="switchIP"
      :mac="switchMAC"
      :snmpCommunity="switchSNMPCommunity"
      @submit="handleSubmitSwitch"
      @close="closeSwitchForm"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, Ref } from "@vue/composition-api";

import FloorCard from "../components/cards/FloorCard.vue";
import Confirmation from "../components/Confirmation.vue";
import FloorForm from "../components/forms/FloorForm.vue";
import SwitchForm from "../components/forms/SwitchForm.vue";

import { FloorRequest, FloorResponse } from "../types/floor";
import { SwitchRequest } from "../types/switch";
import { getFloorsOf, addFloor, deleteFloor } from "../api/floors";
import { getBuild } from "../api/builds";
import { addSwitch } from "../api/switches";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
    shortName: { type: String, required: true },
  },

  components: {
    FloorCard,
    Confirmation,
    FloorForm,
    SwitchForm,
  },

  setup() {
    const floors: Ref<FloorResponse[]> = ref([]);

    const confirmation = ref(false);
    const deleteName = ref("");

    const floorForm = ref(false);
    const floor: Ref<FloorRequest> = ref({} as FloorRequest);

    const sw = ref({} as SwitchRequest);
    const switchForm = ref(false);
    const switchFormAction = ref("");
    const switchName = ref("");
    const switchIPResolveMethod = ref("DNS");
    const switchIP = ref("");
    const switchMAC = ref("");
    const switchSNMPCommunity = ref("public");

    return {
      floors,

      confirmation,
      deleteName,

      floorForm,
      floor,

      sw,
      switchForm,
      switchFormAction,
      switchName,
      switchIPResolveMethod,
      switchIP,
      switchMAC,
      switchSNMPCommunity,

      deleteFloor,
    };
  },

  methods: {
    handleDelete(floor: FloorResponse) {
      this.deleteName = `Floor ${floor.number}`;
      this.floor = { number: floor.number } as FloorRequest;
      this.confirmation = true;
    },

    handleAddSwitch(shortName: string, floor: FloorResponse) {
      this.sw = {
        name: "",
        ip: "",
        mac: "",
        buildShortName: shortName,
        floorNumber: floor.number,
      } as SwitchRequest;
      this.switchFormAction = "Add";
      this.switchForm = true;
    },

    openFloorForm() {
      this.floorForm = true;
      this.floor = {} as FloorRequest;
    },

    handleSubmitFloor(number: number) {
      try {
        getBuild(this.shortName).then((b) => {
          addFloor({
            number,
            buildName: b.name,
            buildShortName: this.shortName,
          } as FloorRequest);
          this.closeFloorForm();
        });
      } catch (error: any) {
        console.log(error);
      }
    },

    closeFloorForm() {
      this.floor = {} as FloorRequest;
      this.floorForm = false;
    },

    handleSubmitSwitch(
      name: string,
      ipResolveMethod: string,
      ip: string,
      mac: string,
      snmpCommunity: string,
      _build: string,
      _floor: string,
      retrieveFromNetdata: boolean,
      action: string
    ) {
      try {
        switch (retrieveFromNetdata) {
          case false:
            addSwitch({} as SwitchRequest);
            break;
          default:
            break;
        }
        console.log(name);
        console.log(ipResolveMethod);
        console.log(ip);
        console.log(mac);
        console.log(snmpCommunity);
        console.log(_build);
        console.log(_floor);
        console.log(retrieveFromNetdata);
        console.log(action);
        this.switchForm = false;
      } catch (error: any) {
        console.log(error);
      }
    },

    closeSwitchForm() {
      this.switchFormAction = "";
      this.switchForm = false;
      this.switchName = "";
      this.switchIPResolveMethod = "DNS";
      this.switchIP = "";
      this.switchMAC = "";
      this.switchSNMPCommunity = "public";
    },
  },

  created() {
    getFloorsOf(this.shortName).then((floors) => (this.floors = floors));
  },
});
</script>
