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
          <v-text-field v-model="inputName" label="Name" color="orange accent-2" required></v-text-field>

          <v-row dense>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inputMAC"
                label="MAC"
                placeholder="XX:XX:XX:XX:XX:XX"
                color="orange accent-2"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-text-field
                v-model="inputSNMPCommunity"
                label="SNMP community"
                color="orange accent-2"
                required
              ></v-text-field>
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
              <v-text-field
                v-model="inputIP"
                label="IP"
                placeholder="e.g. 192.168.1.1"
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
        <v-btn color="orange darken-1" @click="submit">{{ action }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";
import { mdiClose } from "@mdi/js";

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