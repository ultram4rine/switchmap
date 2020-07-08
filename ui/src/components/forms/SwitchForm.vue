<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="$emit('close')">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <v-text-field v-model="inputName" label="Name" color="orange accent-2" required></v-text-field>

          <v-text-field
            v-model="inputMAC"
            label="MAC"
            placeholder="XX:XX:XX:XX:XX:XX"
            color="orange accent-2"
            required
          ></v-text-field>

          <v-row dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="inputIPResolveMethod"
                :items="methods"
                hide-details
                label="IP resolve method"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
            <v-col v-if="inputIPResolveMethod === 'Direct'" cols="12" sm="6">
              <v-text-field
                v-model="inputIP"
                label="IP"
                placeholder="e.g. 192.168.1.1"
                color="orange accent-2"
                required
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="inputSNMPCommunityType"
                :items="types"
                hide-details
                label="SNMP community type"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
            <v-col v-if="inputSNMPCommunityType === 'Private'" cols="12" sm="6">
              <v-text-field
                v-model="inputSNMPCommunity"
                label="Community"
                color="orange accent-2"
                required
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row v-if="needLocationFields" dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="inputBuild"
                :items="builds"
                hide-details
                label="Build"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
            <v-col cols="12" sm="6">
              <v-select
                v-model="inputFloor"
                :items="floors"
                hide-details
                label="Floor"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('submit')">{{ action }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {
  defineComponent,
  Ref,
  ref,
  computed,
  watch
} from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import useInputValidator from "@/helpers/useInputValidator";

import { isMAC, isIP } from "@/validators";

import { Build, Floor } from "@/interfaces";

import { getAllBuilds, getFloorsOf } from "@/helpers/getting";

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },
    needLocationFields: { type: Boolean, required: true },
    name: { type: String, required: true },
    mac: { type: String, required: true },
    ipResolveMethod: { type: String, required: true },
    ip: { type: String, required: true },
    snmpCommunityType: { type: String, required: true },
    snmpCommunity: { type: String, required: true },
    build: { type: String, required: true },
    floor: { type: Number, required: true }
  },

  setup(props, { emit }) {
    const title = computed(() => {
      if (props.action == "Add") return "New switch";
      else if (props.action == "Change") return "Change switch";
    });

    const inputName = useInputValidator(props.name, [], (name: string) =>
      emit("input", name)
    );
    const inputMAC = useInputValidator(props.mac, [], (mac: string) =>
      emit("input", mac)
    );
    const inputIPResolveMethod = useInputValidator(
      props.ipResolveMethod,
      [],
      (ipResolveMethod: string) => emit("input", ipResolveMethod)
    );
    const inputIP = useInputValidator(props.ip, [], (ip: string) =>
      emit("input", ip)
    );
    const inputSNMPCommunityType = useInputValidator(
      props.snmpCommunityType,
      [],
      (snmpCommunityType: string) => emit("input", snmpCommunityType)
    );
    const inputSNMPCommunity = useInputValidator(
      props.snmpCommunity,
      [],
      (snmpCommunity: string) => emit("input", snmpCommunity)
    );
    const inputBuild = useInputValidator(props.build, [], (build: string) =>
      emit("input", build)
    );
    const inputFloor = useInputValidator(
      props.floor.toString(),
      [],
      (floor: string) => emit("input", floor)
    );

    const builds = computed(getAllBuilds);
    let floors: Ref<Floor[]> = ref([]);
    watch(inputBuild, value => {
      floors.value = getFloorsOf(inputBuild.input.value);
    });

    return {
      title,

      inputName,
      inputMAC,
      inputIPResolveMethod,
      inputIP,
      inputSNMPCommunityType,
      inputSNMPCommunity,
      inputBuild,
      inputFloor,

      builds,
      floors,

      mdiClose
    };
  }
});
</script>