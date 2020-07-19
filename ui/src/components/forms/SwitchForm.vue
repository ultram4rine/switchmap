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
            <ValidationProvider
              v-slot="{ errors }"
              name="Name"
              rules="required"
            >
              <v-text-field
                v-model="inputName"
                :error-messages="errors"
                label="Name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>

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
                <ValidationProvider
                  v-slot="{ errors }"
                  name="IP address"
                  rules="required|ip"
                >
                  <v-text-field
                    v-model="inputIP"
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
              <v-col cols="12" sm="6">
                <ValidationProvider
                  v-slot="{ errors }"
                  name="MAC address"
                  rules="required|mac"
                >
                  <v-text-field
                    v-model="inputMAC"
                    :error-messages="errors"
                    label="MAC"
                    placeholder="XX:XX:XX:XX:XX:XX"
                    required
                    color="orange accent-2"
                  ></v-text-field>
                </ValidationProvider>
              </v-col>
              <v-col cols="12" sm="6">
                <ValidationProvider
                  v-slot="{ errors }"
                  name="SNMP community"
                  rules="required"
                >
                  <v-text-field
                    v-model="inputSNMPCommunity"
                    :error-messages="errors"
                    label="SNMP community"
                    required
                    color="orange accent-2"
                  ></v-text-field>
                </ValidationProvider>
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
          <v-btn color="orange darken-1" :disabled="invalid" @click="submit">
            {{ action }}
          </v-btn>
        </v-card-actions>
      </ValidationObserver>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
/* eslint-disable no-unused-vars */
import {
  defineComponent,
  ref,
  computed,
  watch,
  Ref,
} from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { Build, Floor } from "@/interfaces";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

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
    const regex = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/g;
    return regex.test(val);
  },
  message: "{_value_} is not correct IP address",
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },
    needLocationFields: { type: Boolean, required: true },

    name: { type: String, required: true },
    ipResolveMethod: { type: String, required: true },
    ip: { type: String, required: true },
    mac: { type: String, required: true },
    snmpCommunity: { type: String, required: true },
    build: { type: String, required: false },
    floor: { type: String, required: false },
  },

  components: {
    ValidationObserver,
    ValidationProvider,
  },

  setup(props, { emit }) {
    const title = computed(() => {
      if (props.action == "Add") return "New build";
      else if (props.action == "Change") return "Change build";
    });

    const inputName = ref(props.name);
    const inputIPResolveMethod = ref(props.ipResolveMethod);
    const inputIP = ref(props.ip);
    const inputMAC = ref(props.mac);
    const inputSNMPCommunity = ref(props.snmpCommunity);
    const inputBuild = ref(props.build);
    const inputFloor = ref(props.floor);

    const methods = ["Direct", "DNS"];
    const builds: Ref<Build[]> = ref([]);
    const floors: Ref<Floor[]> = ref([]);

    watch(
      () => props.name,
      val => {
        inputName.value = val;
      }
    );
    watch(
      () => props.ipResolveMethod,
      val => {
        inputIPResolveMethod.value = val;
      }
    );
    watch(
      () => props.ip,
      val => {
        inputIP.value = val;
      }
    );
    watch(
      () => props.mac,
      val => {
        inputMAC.value = val;
      }
    );
    watch(
      () => props.snmpCommunity,
      val => {
        inputSNMPCommunity.value = val;
      }
    );
    watch(
      () => props.build,
      val => {
        inputBuild.value = val;
      }
    );
    watch(
      () => props.floor,
      val => {
        inputFloor.value = val;
      }
    );

    const submit = () => {
      emit(
        "submit",
        inputName.value,
        inputIPResolveMethod.value,
        inputIP.value,
        inputMAC.value,
        inputSNMPCommunity.value,
        inputBuild.value,
        inputFloor.value
      );
    };
    const close = () => emit("close");

    return {
      title,
      inputName,
      inputIPResolveMethod,
      inputIP,
      inputMAC,
      inputSNMPCommunity,
      inputBuild,
      inputFloor,

      methods,
      builds,
      floors,

      submit,
      close,

      mdiClose,
    };
  },
});
</script>
