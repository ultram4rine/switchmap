<template>
  <div id="vis">
    <v-toolbar dense></v-toolbar>
    <div id="container"></div>
  </div>
</template>

<script lang="ts">
/* eslint-disable no-unused-vars */
import { defineComponent, ref } from "@vue/composition-api";

import { Node, Edge, Network } from "vis-network/standalone";

import useSwitches from "@/helpers/useSwitches";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  setup() {
    const showAll = ref(false);

    const { switches, getAllSwitches } = useSwitches();

    const displaySwitches = () => {
      const container = document.getElementById("container") as HTMLElement;

      const nodes = new Array<Node>();
      const edges = new Array<Edge>();
      switches.value.forEach((sw) => {
        nodes.push({ id: sw.name, label: sw.name });
        edges.push({ from: sw.upSwitchName, to: sw.name, label: sw.upLink });
      });

      const data = {
        nodes: nodes,
        edges: edges,
      };

      const options = {
        physics: { enabled: false },
        nodes: {
          physics: false,
          color: "rgb(255, 140, 0)",
          font: { color: "#181616", face: "sans-serif" },
          shape: "box",
        },
        edges: {
          arrows: { to: { enabled: true } },
          color: { color: "#181616" },
        },
      };

      const network = new Network(container, data, options);
    };

    return {
      showAll,

      switches,
      getAllSwitches,

      displaySwitches,
    };
  },

  created() {
    this.getAllSwitches().then((sws) => {
      this.switches = sws;
      this.displaySwitches();
    });
  },
});
</script>
