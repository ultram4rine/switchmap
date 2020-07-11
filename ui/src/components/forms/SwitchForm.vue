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

      <v-card-text>
        <v-form ref="form">
          <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
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
              <ValidationProvider v-slot="{ errors }" name="MAC address" rules="required|mac">
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
              <ValidationProvider v-slot="{ errors }" name="SNMP community" rules="required">
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
        <v-btn color="orange darken-1" @click="submit">{{ action }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";
import { mdiClose } from "@mdi/js";

import { ValidationProvider, extend } from "vee-validate";
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

export default Vue.extend({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },
    needLocationFields: { type: Boolean, required: true },

    name: { type: String, required: true },
    mac: { type: String, required: true },
    snmpCommunity: { type: String, required: true },
    ipResolveMethod: { type: String, required: true },
    ip: { type: String, required: true },
    build: { type: String, required: true },
    floor: { type: String, required: true }
  },

  components: {
    ValidationProvider
  },

  data() {
    return {
      mdiClose: mdiClose,

      inputName: this.name,
      inputMAC: this.mac,
      inputSNMPCommunity: this.snmpCommunity,
      inputIPResolveMethod: this.ipResolveMethod,
      inputIP: this.ip,
      inputBuild: this.build,
      inputFloor: this.floor,

      methods: ["Direct", "DNS"],
      types: ["Public", "Private"],

      builds: [],
      floors: []
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New switch";
      else if (this.action == "Change") return "Change switch";
    }
  },

  watch: {
    name: function(newName) {
      this.inputName = newName;
    },
    mac: function(newMAC) {
      this.inputMAC = newMAC;
    },
    snmpCommunity: function(newSNMPCommunity) {
      this.inputSNMPCommunity = newSNMPCommunity;
    },
    ipResolveMethod: function(newIPResolveMethod) {
      this.inputIPResolveMethod = newIPResolveMethod;
    },
    ip: function(newIP) {
      this.inputIP = newIP;
    },
    build: function(newBuild) {
      this.inputBuild = newBuild;
    },
    floor: function(newFloor) {
      this.inputFloor = newFloor;
    }
  },

  methods: {
    submit() {
      this.$emit(
        "submit",
        this.inputName,
        this.inputMAC,
        this.inputSNMPCommunity,
        this.inputIPResolveMethod,
        this.inputIP,
        this.inputBuild,
        this.inputFloor
      );
    },
    close() {
      this.$emit("close");
    }
  }
});
</script>