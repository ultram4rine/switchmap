import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "@/config";
import { Floor } from "@/interfaces";

const floorsMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      item: "",
      snackbarAction: "",

      floors: new Array<Floor>(),
      floorsEndpoint: (build: string) => {
        return `${config.apiURL}/builds/${build}/floors`;
      },
      floorEndpoint: (build: string, floor: string) => {
        return `${config.apiURL}/builds/${build}/${floor}`;
      },

      floorForm: false,

      floorNumber: "",
      floorBuildName: "",
      floorBuildShortName: "",
    };
  },

  methods: {
    getFloorsOf(build: string) {
      axios
        .get<Floor, AxiosResponse<Floor[]>>(this.floorsEndpoint(build))
        .then((resp) => (this.floors = resp.data))
        .catch((err) => console.log(err));
    },

    addFloorTo(build: string) {
      axios
        .post(this.floorsEndpoint(build), {
          number: parseInt(this.floorNumber, 10),
        })
        .then(() => {
          this.floorForm = false;

          this.item = `${this.floorNumber} floor in ${this.floorBuildName}`;
          this.snackbarAction = "added";
          this.snackbar = true;

          this.floorNumber = "";
          this.floorBuildName = "";
        })
        .catch((err) => console.log(err));
    },

    deleteFloorOf(build: string, floor: string) {
      axios.delete(this.floorEndpoint(build, floor)).then(() => {
        this.getFloorsOf(build);

        this.item = `${floor} floor in ${build}`;
        this.snackbarAction = "deleted";
        this.snackbar = true;
      });
    },

    closeFloorForm() {
      this.floorForm = false;
      this.floorNumber = "";
      this.floorBuildName = "";
      this.floorBuildShortName = "";
    },

    updateSnackbar(snackbar: boolean) {
      this.snackbar = snackbar;
      this.item = "";
      this.snackbarAction = "";
    },
  },
});

export default floorsMixin;
