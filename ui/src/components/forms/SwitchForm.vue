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
          <v-text-field v-model="name" label="Name" color="orange accent-2" required></v-text-field>

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
              <v-text-field
                v-model="ip"
                label="IP"
                placeholder="e.g. 192.168.1.1"
                color="orange accent-2"
                required
              ></v-text-field>
            </v-col>
          </v-row>

          <v-text-field
            v-model="mac"
            label="MAC"
            placeholder="XX:XX:XX:XX:XX:XX"
            color="orange accent-2"
            required
          ></v-text-field>

          <v-row dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="snmpCommunityType"
                :items="types"
                hide-details
                label="SNMP community type"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
            <v-col v-if="snmpCommunityType === 'Private'" cols="12" sm="6">
              <v-text-field
                v-model="snmpCommunity"
                label="Community"
                color="orange accent-2"
                required
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row v-if="needLocationFields" dense>
            <v-col cols="12" sm="6">
              <v-select
                v-model="build"
                :items="builds"
                hide-details
                label="Build"
                color="orange accent-2"
                required
              ></v-select>
            </v-col>
            <v-col cols="12" sm="6">
              <v-select
                v-model="floor"
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
import { mdiClose } from "@mdi/js";

import mixins from "vue-typed-mixins";

import switchesMixin from "../../mixins/switchesMixin";

export default mixins(switchesMixin).extend({
  props: {
    form: { type: Boolean, required: true },
    needLocationFields: { type: Boolean, required: true }
  },

  data() {
    return {
      mdiClose: mdiClose,

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
  }
});
</script>