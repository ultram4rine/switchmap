<template>
  <div id="vis">
    <v-toolbar>
      <v-autocomplete
        v-model="show"
        :items="builds"
        :disabled="showAll"
        multiple
        small-chips
        hide-details
        item-text="name"
        item-value="shortName"
        label="Build"
        color="orange accent-2"
        @change="displaySwitches()"
      >
        <template v-slot:selection="data">
          <v-chip
            v-bind="data.attrs"
            :input-value="data.selected"
            small
            close
            @click:close="remove(data.item), displaySwitches()"
          >
            {{ data.item.name }}
          </v-chip>
        </template>
      </v-autocomplete>
      <v-divider class="mx-4" vertical></v-divider>
      <v-checkbox
        v-model="showAll"
        label="Show all"
        hide-details
        color="orange darken-1"
        @change="displaySwitches()"
      ></v-checkbox>
    </v-toolbar>
    <div id="container"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref } from "vue";

import {
  Network,
  DataSet,
  DataSetNodes,
  DataSetEdges,
} from "vis-network/standalone";

import { SwitchResponse, BuildResponse } from "@/interfaces";
import { getSwitches } from "@/api/switches";
import { getBuilds } from "@/api/builds";

export default defineComponent({
  props: {
    isLoading: { type: Boolean, required: true },
  },

  setup() {
    const switchesAll: Ref<SwitchResponse[]> = ref([]);
    const switches: Ref<SwitchResponse[]> = ref([]);

    const builds: Ref<BuildResponse[]> = ref([]);

    const show: Ref<string[]> = ref([]);
    const showAll = ref(true);

    return {
      switchesAll,
      switches,

      builds,

      show,
      showAll,
    };
  },

  methods: {
    displaySwitches() {
      const container = document.getElementById("container") as HTMLElement;

      const nodes: DataSetNodes = new DataSet();
      const edges: DataSetEdges = new DataSet();
      if (this.showAll) {
        this.switches = this.switchesAll;
      } else {
        this.switches = this.switchesAll.filter(
          (sw) => this.show.indexOf(sw.buildShortName) !== -1
        );
      }
      this.switches.forEach((sw) => {
        nodes.add({ id: sw.name, label: sw.name });
        if (sw.upSwitchName) {
          edges.add({ from: sw.upSwitchName, to: sw.name, label: sw.upLink });
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

      new Network(container, data, options);
    },

    remove(b: BuildResponse) {
      const index = this.show.indexOf(b.shortName);
      if (index >= 0) this.show.splice(index, 1);
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
