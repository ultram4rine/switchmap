<template>
  <v-dialog :value="value" persistent max-width="500px">
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
          <v-text-field
            v-model="switchName"
            label="Name"
            color="orange accent-2"
            required
            v-on:keyup="emitSwitchName"
          ></v-text-field>
          <v-row dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="switchIPResolveMethod"
                :items="methods"
                hide-details
                label="IP resolve method"
                color="orange accent-2"
                required
                v-on:input="emitSwitchIPResolveMethod"
              ></v-select>
            </v-col>
            <v-col v-if="switchIPResolveMethod === 'Direct'" cols="12" sm="6">
              <v-text-field
                v-model="switchIP"
                label="IP"
                placeholder="e.g. 192.168.1.1"
                color="orange accent-2"
                required
                v-on:keyup="emitSwitchIP"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-text-field
            v-model="switchMAC"
            label="MAC"
            placeholder="XX:XX:XX:XX:XX:XX"
            color="orange accent-2"
            required
            v-on:keyup="emitSwitchMAC"
          ></v-text-field>
          <v-row dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="switchSNMPCommunityType"
                :items="types"
                hide-details
                label="SNMP community type"
                color="orange accent-2"
                required
                v-on:input="emitSwitchSNMPCommunityType"
              ></v-select>
            </v-col>
            <v-col v-if="switchSNMPCommunityType === 'Private'" cols="12" sm="6">
              <v-text-field
                v-model="switchSNMPCommunity"
                label="Community"
                color="orange accent-2"
                required
                v-on:keyup="emitSwitchSNMPCommunity"
              ></v-text-field>
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
import Vue from "vue";

import { mdiClose } from "@mdi/js";

export default Vue.extend({
  props: {
    value: { type: Boolean, required: true },
    action: { type: String, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose,

      switchName: "",
      switchIPResolveMethod: "Direct",
      switchIP: "",
      switchMAC: "",
      switchSNMPCommunityType: "Public",
      switchSNMPCommunity: "",

      methods: ["Direct", "DNS"],
      types: ["Public", "Private"]
    };
  },

  computed: {
    title: function() {
      if (this.action == "Add") return "New switch";
      else if (this.action == "Change") return "Change switch";
    }
  },

  methods: {
    emitSwitchName() {
      this.$emit("emitSwitchName", this.switchName);
    },
    emitSwitchIPResolveMethod() {
      this.$emit("emitSwitchIPResolveMethod", this.switchIPResolveMethod);
    },
    emitSwitchIP() {
      this.$emit("emitSwitchIP", this.switchIP);
    },
    emitSwitchMAC() {
      this.$emit("emitSwitchMAC", this.switchMAC);
    },
    emitSwitchSNMPCommunityType() {
      this.$emit("emitSwitchSNMPCommunityType", this.switchSNMPCommunityType);
    },
    emitSwitchSNMPCommunity() {
      this.$emit("emitSwitchSNMPCommunity", this.switchSNMPCommunity);
    }
  }
});
</script>