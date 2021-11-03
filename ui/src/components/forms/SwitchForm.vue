<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <ValidationObserver ref="observer" v-slot="{ invalid }">
        <v-card-text>
          <v-form ref="form">
            <v-checkbox
              v-model="retrieveFromNetData"
              label="Retrieve switch data from netdata"
              color="orange accent-2"
            ></v-checkbox>

            <ValidationProvider
              v-slot="{ errors }"
              name="Name"
              rules="required"
            >
              <v-text-field
                v-model="name"
                :error-messages="errors"
                label="Name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>

            <template v-if="!retrieveFromNetData">
              <v-row dense>
                <v-col cols="12" sm="6">
                  <v-select
                    v-model="ipResolveMethod"
                    :items="methods"
                    hide-details
                    label="IP resolve method"
                    color="orange accent-2"
                    required
                  ></v-select>
                </v-col>
                <v-col v-if="ipResolveMethod === 'Direct'" cols="12" sm="6">
                  <ValidationProvider
                    v-slot="{ errors }"
                    name="IP address"
                    rules="required|ip"
                  >
                    <v-text-field
                      v-model="ip"
                      :error-messages="errors"
                      label="IP"
                      placeholder="e.g. 192.168.1.1"
                      required
                      color="orange accent-2"
                    ></v-text-field>
                  </ValidationProvider>
                </v-col>
              </v-row>

              <v-row dense>
                <v-col cols="12" sm="12">
                  <ValidationProvider
                    v-slot="{ errors }"
                    name="MAC address"
                    rules="required|mac"
                  >
                    <v-text-field
                      v-model="mac"
                      :error-messages="errors"
                      label="MAC"
                      placeholder="XX:XX:XX:XX:XX:XX"
                      required
                      color="orange accent-2"
                    ></v-text-field>
                  </ValidationProvider>
                </v-col>
              </v-row>
            </template>

            <v-checkbox
              v-model="retrieveUpLinkFromSeens"
              label="Retrieve uplink from seens"
              color="orange accent-2"
            ></v-checkbox>
            <v-row v-if="!retrieveUpLinkFromSeens">
              <v-col cols="12" sm="6">
                <v-autocomplete
                  v-model="upSwitchName"
                  :items="switches"
                  hide-details
                  item-text="name"
                  item-value="name"
                  label="Up switch"
                  color="orange accent-2"
                  required
                ></v-autocomplete>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="upLink"
                  label="Up link"
                  color="orange accent-2"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-checkbox
              v-model="retrieveTechDataFromSNMP"
              label="Retrieve tech data from SNMP"
              color="orange accent-2"
            ></v-checkbox>
            <v-row v-if="!retrieveTechDataFromSNMP">
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="revision"
                  label="Revision"
                  color="orange accent-2"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="serial"
                  label="Serial"
                  color="orange accent-2"
                ></v-text-field>
              </v-col>
            </v-row>
            <ValidationProvider
              v-slot="{ errors }"
              v-else
              name="SNMP community"
              rules="required"
            >
              <v-select
                v-model="snmpCommunity"
                :error-messages="errors"
                :items="communitites"
                label="SNMP community"
                required
                color="orange accent-2"
              ></v-select>
            </ValidationProvider>

            <v-row v-if="needLocationFields" dense>
              <v-col cols="12" sm="6">
                <v-autocomplete
                  v-model="build"
                  :items="builds"
                  hide-details
                  item-text="name"
                  item-value="shortName"
                  label="Build"
                  color="orange accent-2"
                  required
                  @change="getFloors(build)"
                ></v-autocomplete>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="floor"
                  :items="floors"
                  hide-details
                  item-text="number"
                  item-value="number"
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
          <v-btn color="orange darken-1" :disabled="invalid" @click="submit">
            {{ action }}
          </v-btn>
        </v-card-actions>
      </ValidationObserver>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  computed,
  watch,
  Ref,
  PropType,
} from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

import { getSNMPCommunities, getSwitches } from "../../api/switches";
import { getBuilds } from "../../api/builds";
import { getFloorsOf } from "../../api/floors";

import { SwitchRequest, SwitchResponse } from "../../types/switch";
import { BuildResponse } from "../../types/build";
import { FloorResponse } from "../../types/floor";

import { macNormalization } from "../../helpers";

extend("required", {
  ...required,
  message: "{_field_} is required",
});

extend("mac", {
  validate: (val: string) => {
    const regex = /^[a-fA-F0-9:]{17}|[a-fA-F0-9]{12}$/g;
    return regex.test(val);
  },
  message: "{_value_} is not correct MAC address",
});

extend("ip", {
  validate: (val: string) => {
    const regex =
      /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/g;
    return regex.test(val);
  },
  message: "{_value_} is not correct IP address",
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },

    sw: { type: Object as PropType<SwitchRequest>, required: true },

    needLocationFields: { type: Boolean, required: true },
  },

  components: {
    ValidationObserver,
    ValidationProvider,
  },

  setup(props, { emit }) {
    const title = computed(() => {
      return props.action == "Add" ? "New switch" : "Change switch";
    });

    const retrieveFromNetData = ref(props.sw.retrieveFromNetData);
    const retrieveUpLinkFromSeens = ref(props.sw.retrieveUpLinkFromSeens);
    const retrieveTechDataFromSNMP = ref(props.sw.retrieveTechDataFromSNMP);
    const name = ref(props.sw.name);
    const ipResolveMethod = ref(props.sw.ipResolveMethod);
    const ip = ref(props.sw.ip);
    const mac = ref(props.sw.mac);
    const upSwitchName = ref(props.sw.upSwitchName);
    const upLink = ref(props.sw.upLink);
    const build = ref(props.sw.buildShortName);
    const floor = ref(props.sw.floorNumber);
    const snmpCommunity = ref(props.sw.snmpCommunity);
    const revision = ref(props.sw.revision);
    const serial = ref(props.sw.serial);

    const methods = ["Direct", "DNS"];
    const communitites: Ref<string[]> = ref([]);
    const switches: Ref<SwitchResponse[]> = ref([]);
    const builds: Ref<BuildResponse[]> = ref([]);
    const floors: Ref<FloorResponse[]> = ref([]);

    watch(
      () => props.sw.retrieveFromNetData,
      (val) => {
        retrieveFromNetData.value = val;
      }
    );
    watch(
      () => props.sw.retrieveUpLinkFromSeens,
      (val) => {
        retrieveUpLinkFromSeens.value = val;
      }
    );
    watch(
      () => props.sw.retrieveTechDataFromSNMP,
      (val) => {
        retrieveTechDataFromSNMP.value = val;
      }
    );
    watch(
      () => props.sw.name,
      (val) => {
        name.value = val;
      }
    );
    watch(
      () => props.sw.ipResolveMethod,
      (val) => {
        ipResolveMethod.value = val;
      }
    );
    watch(
      () => props.sw.ip,
      (val) => {
        ip.value = val;
      }
    );
    watch(
      () => props.sw.mac,
      (val) => {
        mac.value = val;
      }
    );
    watch(
      () => props.sw.upSwitchName,
      (val) => {
        upSwitchName.value = val;
      }
    );
    watch(
      () => props.sw.upLink,
      (val) => {
        upLink.value = val;
      }
    );
    watch(
      () => props.sw.buildShortName,
      (val) => {
        build.value = val;
        if (val) getFloorsOf(build.value).then((fs) => (floors.value = fs));
      }
    );
    watch(
      () => props.sw.floorNumber,
      (val) => {
        floor.value = val;
      }
    );
    watch(
      () => props.sw.snmpCommunity,
      (val) => {
        snmpCommunity.value = val;
      }
    );
    watch(
      () => props.sw.revision,
      (val) => {
        revision.value = val;
      }
    );
    watch(
      () => props.sw.serial,
      (val) => {
        serial.value = val;
      }
    );

    const submit = () => {
      emit(
        "submit",
        name.value,
        ipResolveMethod.value,
        ip.value,
        mac.value.length === 12 ? mac.value : macNormalization(mac.value),
        upSwitchName.value,
        upLink.value,
        snmpCommunity.value,
        revision.value,
        serial.value,
        build.value,
        floor.value,
        retrieveFromNetData.value,
        retrieveUpLinkFromSeens.value,
        retrieveTechDataFromSNMP.value,
        props.action
      );
    };
    const close = () => emit("close");

    return {
      title,

      retrieveFromNetData,
      retrieveUpLinkFromSeens,
      retrieveTechDataFromSNMP,
      name,
      ipResolveMethod,
      ip,
      mac,
      upSwitchName,
      upLink,
      build,
      floor,
      snmpCommunity,
      revision,
      serial,

      methods,
      communitites,
      switches,
      builds,
      floors,

      submit,
      close,

      mdiClose,
    };
  },

  methods: {
    getFloors(build: string) {
      getFloorsOf(build).then((floors) => (this.floors = floors));
    },
  },

  created() {
    getSNMPCommunities().then((comms: string[]) => {
      this.communitites = comms;
      if (!this.sw.snmpCommunity) this.snmpCommunity = this.communitites[0];
    });
    getSwitches().then((switches) => (this.switches = switches));
    if (this.needLocationFields) {
      getBuilds().then((builds) => (this.builds = builds));
    }
  },
});
</script>
