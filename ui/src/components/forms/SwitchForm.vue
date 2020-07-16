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
            <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
              <v-text-field
                v-model="inputSwitch.name"
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
                <ValidationProvider v-slot="{ errors }" name="IP address" rules="required|ip">
                  <v-text-field
                    v-model="inputSwitch.ip"
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
                <ValidationProvider v-slot="{ errors }" name="MAC address" rules="required|mac">
                  <v-text-field
                    v-model="inputSwitch.mac"
                    :error-messages="errors"
                    label="MAC"
                    placeholder="XX:XX:XX:XX:XX:XX"
                    required
                    color="orange accent-2"
                  ></v-text-field>
                </ValidationProvider>
              </v-col>
              <v-col cols="12" sm="6">
                <ValidationProvider v-slot="{ errors }" name="SNMP community" rules="required">
                  <v-text-field
                    v-model="inputSwitch.snmpCommunity"
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
          <v-btn color="orange darken-1" :disabled="invalid" @click="submit">{{ action }}</v-btn>
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
  Ref
} from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { Build, Floor, Switch } from "@/interfaces";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

extend("required", {
  ...required,
  message: "{_field_} is required"
});

extend("mac", {
  validate: (val: string) => {
    let regex = /^[a-fA-F0-9:]{17}|[a-fA-F0-9]{12}$/g;
    return regex.test(val);
  },
  message: "{_value_} is not correct MAC address"
});

extend("ip", {
  validate: (val: string) => {
    let regex = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/g;
    return regex.test(val);
  },
  message: "{_value_} is not correct IP address"
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },
    needLocationFields: { type: Boolean, required: true },

    switch: { type: Object as () => Switch, required: true },
    ipResolveMethod: { type: String, required: true },
    build: { type: String, required: true },
    floor: { type: String, required: true }
  },

  components: {
    ValidationObserver,
    ValidationProvider
  },

  setup(props, { emit }) {
    const title = computed(() => {
      if (props.action == "Add") return "New build";
      else if (props.action == "Change") return "Change build";
    });

    const inputSwitch = ref(props.switch);
    const inputIPResolveMethod = ref(props.ipResolveMethod);
    const inputBuild = ref(props.build);
    const inputFloor = ref(props.floor);

    const methods = ["Direct", "DNS"];
    const builds: Ref<Build[]> = ref([]);
    const floors: Ref<Floor[]> = ref([]);

    watch(
      () => props.switch.name,
      (val: string) => {
        inputSwitch.value.name = val;
      }
    );
    watch(
      () => props.ipResolveMethod,
      (val: string) => {
        inputIPResolveMethod.value = val;
      }
    );
    watch(
      () => props.switch.ip,
      (val: string) => {
        inputSwitch.value.ip = val;
      }
    );
    watch(
      () => props.switch.mac,
      (val: string) => {
        inputSwitch.value.mac = val;
      }
    );
    watch(
      () => props.switch.snmpCommunity,
      (val: string) => {
        inputSwitch.value.snmpCommunity = val;
      }
    );
    watch(
      () => props.build,
      (val: string) => {
        inputBuild.value = val;
      }
    );
    watch(
      () => props.floor,
      (val: string) => {
        inputFloor.value = val;
      }
    );

    const submit = () => {
      emit("submit", inputSwitch.value);
    };
    const close = () => emit("close");

    return {
      title,
      inputSwitch,
      inputIPResolveMethod,

      methods,
      builds,
      floors,

      submit,
      close,

      mdiClose
    };
  }
});
</script>