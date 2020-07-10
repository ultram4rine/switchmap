<template>
  <div id="vis">
    <v-toolbar dense></v-toolbar>
    <div id="container"></div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { Node, Edge, Network } from "vis-network/standalone";

import { config } from "@/config";
import { Switch } from "@/interfaces";

export default Vue.extend({
  props: {
    isLoading: { type: Boolean, required: true }
  },

  data() {
    return {
      switches: new Array<Switch>(),
      switchesEndpoint: `${config.apiURL}/switches`,

      showAll: false
    };
  },

  created() {
    this.displaySwitches();
  },

  methods: {
    displaySwitches() {
      axios
        .get<Switch, AxiosResponse<Switch[]>>(this.switchesEndpoint)
        .then(resp => {
          this.switches = resp.data;

          let container = <HTMLElement>document.getElementById("container");

          let nodes = new Array<Node>();
          let edges = new Array<Edge>();

          this.switches.forEach(sw => {
            nodes.push({ id: sw.name, label: sw.name });
            edges.push({ from: sw.upSwitch, to: sw.name, label: sw.port });
          });

          let data = {
            nodes: nodes,
            edges: edges
          };

          let options = {
            physics: { enabled: false },
            nodes: {
              physics: false,
              color: "rgb(255, 140, 0)",
              font: { color: "#181616", face: "sans-serif" },
              shape: "box"
            },
            edges: {
              arrows: { to: { enabled: true } },
              color: { color: "#181616" }
            }
          };

          let network = new Network(container, data, options);
        })
        .catch(err => console.log(err));
    }
  }
});
</script>