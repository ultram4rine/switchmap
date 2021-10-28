<template>
  <div id="vis">
    <v-toolbar>
      <v-select
        v-model="show"
        :items="builds"
        hide-details
        item-text="name"
        item-value="shortName"
        label="Build"
        color="orange accent-2"
        required
        @change="displaySwitches()"
      ></v-select>
    </v-toolbar>
    <div id="container"></div>
  </div>
</template>

<script lang="ts">
/* eslint-disable no-unused-vars */
import { defineComponent, Ref, ref } from "@vue/composition-api";

import { Node, Edge, Network } from "vis-network/standalone";

import { SwitchResponse } from "../types/switch";
import { BuildResponse } from "../types/build";
import { getSwitches } from "../api/switches";
import { getBuilds } from "../api/builds";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  setup() {
    const switchesAll: Ref<SwitchResponse[]> = ref([]);
    const switches: Ref<SwitchResponse[]> = ref([]);
    const show = ref("all");
    const builds: Ref<BuildResponse[]> = ref([
      { name: "All", shortName: "all" } as BuildResponse,
    ]);

    return {
      switchesAll,
      switches,
      show,
      builds,
    };
  },

  methods: {
    displaySwitches() {
      const container = document.getElementById("container") as HTMLElement;

      const nodes = new Array<Node>();
      const edges = new Array<Edge>();
      if (this.show == "all") {
        this.switches = this.switchesAll;
      } else {
        this.switches = this.switchesAll.filter(
          (sw) => sw.buildShortName === this.show
        );
      }
      this.switches.forEach((sw) => {
        nodes.push({ id: sw.name, label: sw.name });
        if (sw.upSwitchName) {
          edges.push({ from: sw.upSwitchName, to: sw.name, label: sw.upLink });
        }
      });

      const data = {
        nodes: nodes,
        edges: edges,
      };

      const options = {
        physics: false,
        layout: {
          hierarchical: {
            direction: "UD",
          },
        },
        nodes: {
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
    },
  },

  created() {
    getSwitches().then((sws: SwitchResponse[]) => {
      this.switchesAll = sws;
      this.displaySwitches();
    });
    getBuilds().then((builds) => {
      this.builds = this.builds.concat(builds);
    });
  },
});
</script>

<style>
#container {
  height: calc(100vh - 64px - 64px - 12px - 12px);
}
</style>
