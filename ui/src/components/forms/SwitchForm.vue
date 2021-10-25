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

            <ValidationProvider
              v-slot="{ errors }"
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

            <v-row v-if="needLocationFields" dense>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="build"
                  :items="builds"
                  hide-details
                  item-text="name"
                  item-value="shortName"
                  label="Build"
                  color="orange accent-2"
                  required
                  @change="getFloors(build)"
                ></v-select>
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

import { getSNMPCommunities } from "../../api/switches";
import { getBuilds } from "../../api/builds";
import { getFloorsOf } from "../../api/floors";

import { SwitchRequest } from "../../types/switch";
import { BuildResponse } from "../../types/build";
import { FloorResponse } from "../../types/floor";

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
    const name = ref(props.sw.name);
    const ipResolveMethod = ref(props.sw.ipResolveMethod);
    const ip = ref(props.sw.ip);
    const mac = ref(props.sw.mac);
    const snmpCommunity = ref(props.sw.snmpCommunity);
    const build = ref(props.sw.buildShortName);
    const floor = ref(props.sw.floorNumber);

    const methods = ["Direct", "DNS"];
    const communitites: Ref<string[]> = ref([]);
    const builds: Ref<BuildResponse[]> = ref([]);
    const floors: Ref<FloorResponse[]> = ref([]);

    watch(
      () => props.sw.retrieveFromNetData,
      (val) => {
        retrieveFromNetData.value = val;
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
      () => props.sw.snmpCommunity,
      (val) => {
        snmpCommunity.value = val;
      }
    );
    watch(
      () => props.sw.buildShortName,
      (val) => {
        build.value = val;
      }
    );
    watch(
      () => props.sw.floorNumber,
      (val) => {
        floor.value = val;
      }
    );

    const submit = () => {
      emit(
        "submit",
        name.value,
        ipResolveMethod.value,
        ip.value,
        mac.value,
        snmpCommunity.value,
        build.value,
        floor.value,
        retrieveFromNetData.value,
        props.action
      );
    };
    const close = () => emit("close");

    return {
      title,

      retrieveFromNetData,
      name,
      ipResolveMethod,
      ip,
      mac,
      snmpCommunity,
      build,
      floor,

      methods,
      communitites,
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
    getSNMPCommunities().then((comms: string[]) => (this.communitites = comms));
    if (this.needLocationFields) {
      getBuilds().then((builds) => (this.builds = builds));
    }
  },
});
</script>
