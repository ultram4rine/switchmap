<template>
  <form-wrap :form="form" :title="title" @close="close">
    <ValidationObserver ref="observer" v-slot="{ invalid }">
      <v-form ref="form" @submit.prevent="submit">
        <v-card-text>
          <v-checkbox
            v-model="sw.retrieveFromNetData"
            label="Retrieve switch data from netdata"
            color="orange accent-2"
          ></v-checkbox>

          <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
            <v-text-field
              v-model="sw.name"
              :error-messages="errors"
              label="Name"
              required
              color="orange accent-2"
            ></v-text-field>
          </ValidationProvider>

          <template v-if="!sw.retrieveFromNetData">
            <v-row dense>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="sw.ipResolveMethod"
                  :items="methods"
                  hide-details
                  label="IP resolve method"
                  color="orange accent-2"
                  required
                ></v-select>
              </v-col>
              <v-col v-if="sw.ipResolveMethod === 'Direct'" cols="12" sm="6">
                <ValidationProvider
                  v-slot="{ errors }"
                  name="IP address"
                  rules="required|ip"
                >
                  <v-text-field
                    v-model="sw.ip"
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
                    v-model="sw.mac"
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
            v-model="sw.retrieveUpLinkFromSeens"
            label="Retrieve uplink from seens"
            color="orange accent-2"
          ></v-checkbox>
          <v-row v-if="!sw.retrieveUpLinkFromSeens">
            <v-col cols="12" sm="6">
              <v-autocomplete
                v-model="sw.upSwitchName"
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
                v-model="sw.upLink"
                label="Up link"
                color="orange accent-2"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-checkbox
            v-model="sw.retrieveTechDataFromSNMP"
            label="Retrieve tech data from SNMP"
            color="orange accent-2"
          ></v-checkbox>
          <v-row v-if="!sw.retrieveTechDataFromSNMP">
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="sw.revision"
                label="Revision"
                color="orange accent-2"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="sw.serial"
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
              v-model="sw.snmpCommunity"
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
                v-model="sw.buildShortName"
                :items="builds"
                hide-details
                item-text="name"
                item-value="shortName"
                label="Build"
                color="orange accent-2"
                required
                @change="getFloors(sw.buildShortName)"
              ></v-autocomplete>
            </v-col>
            <v-col v-if="sw.buildShortName" cols="12" sm="6">
              <v-select
                v-model="sw.floorNumber"
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
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn type="submit" color="orange darken-1" :disabled="invalid">
            {{ action }}
          </v-btn>
        </v-card-actions>
      </v-form>
    </ValidationObserver>
  </form-wrap>
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

import FormWrap from "@/components/wrappers/FormWrap.vue";

import { getSNMPCommunities, getSwitches } from "@/api/switches";
import { getBuilds } from "@/api/builds";
import { getFloorsOf } from "@/api/floors";

import {
  SwitchRequest,
  SwitchResponse,
  BuildResponse,
  FloorResponse,
} from "@/interfaces";

extend("required", {
  ...required,
  message: "{_field_} is required",
});

extend("mac", {
  validate: (val: string) => {
    const regex = /^([0-9a-f]{2}[:-]?){5}[0-9a-f]{2}$/i;
    return regex.test(val);
  },
  message: "{_value_} is not correct MAC address",
});

extend("ip", {
  validate: (val: string) => {
    const regex =
      /^((25[0-5]|2[0-4][0-9]|[1]?[0-9][0-9]?)(\.)){3}(25[0-5]|2[0-4][0-9]|[1]?[0-9][0-9]?)$/;
    return regex.test(val);
  },
  message: "{_value_} is not correct IP address",
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true, enum: ["Add", "Edit"] },

    swit: { type: Object as PropType<SwitchRequest>, required: true },

    needLocationFields: { type: Boolean, required: true },
  },

  components: { FormWrap, ValidationObserver, ValidationProvider },

  setup(props, { emit }) {
    const title = computed(() => {
      return props.action === "Add" ? "New switch" : "Change switch";
    });

    const sw = ref({
      retrieveFromNetData: props.swit.retrieveFromNetData,
      retrieveUpLinkFromSeens: props.swit.retrieveUpLinkFromSeens,
      retrieveTechDataFromSNMP: props.swit.retrieveTechDataFromSNMP,
      name: props.swit.name,
      ipResolveMethod: props.swit.ipResolveMethod,
      ip: props.swit.ip,
      mac: props.swit.mac,
      upSwitchName: props.swit.upSwitchName,
      upLink: props.swit.upLink,
      buildShortName: props.swit.buildShortName,
      floorNumber: props.swit.floorNumber,
      snmpCommunity: props.swit.snmpCommunity,
      revision: props.swit.revision,
      serial: props.swit.serial,
    } as SwitchRequest);

    const methods = ["Direct", "DNS"];
    const communitites: Ref<string[]> = ref([]);
    const switches: Ref<SwitchResponse[]> = ref([]);
    const builds: Ref<BuildResponse[]> = ref([]);
    const floors: Ref<FloorResponse[]> = ref([]);

    watch(
      () => props.swit,
      (val) => {
        sw.value.retrieveFromNetData = val.retrieveFromNetData;
        sw.value.retrieveUpLinkFromSeens = val.retrieveUpLinkFromSeens;
        sw.value.retrieveTechDataFromSNMP = val.retrieveTechDataFromSNMP;
        sw.value.name = val.name;
        sw.value.ipResolveMethod = val.ipResolveMethod;
        sw.value.ip = val.ip;
        sw.value.mac = val.mac;
        sw.value.upSwitchName = val.upSwitchName;
        sw.value.upLink = val.upLink;
        sw.value.buildShortName = val.buildShortName;
        sw.value.floorNumber = val.floorNumber;
        sw.value.snmpCommunity = communitites.value[0];
        sw.value.revision = val.revision;
        sw.value.serial = val.serial;
      }
    );

    const submit = () => {
      emit("submit", sw.value, props.action);
    };
    const close = () => emit("close");

    return {
      title,

      sw,

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
      this.sw.snmpCommunity = this.communitites[0];
    });
    getSwitches().then((switches) => (this.switches = switches));
    if (this.needLocationFields) {
      getBuilds().then((builds) => (this.builds = builds));
    }
  },
});
</script>
