import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Floor } from "../interfaces";

const floorsMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      item: "",
      snackbarAction: "",

      floors: new Array<Floor>(),
      floorsEndpoint: `${config.apiURL}/build`,

      floorForm: false,
      addFloorEndpoint: `${config.apiURL}/floor`,

      floorNumber: "",
      floorBuildName: "",
      floorBuildShortName: "",
    };
  },

  methods: {
    getFloorsOf(build: String) {
      axios
        .get<Floor, AxiosResponse<Floor[]>>(
          `${this.floorsEndpoint}/${build}/floors`
        )
        .then((resp) => (this.floors = resp.data))
        .catch((err) => console.log(err));
    },

    addFloor() {
      axios
        .post(this.addFloorEndpoint, {
          number: parseInt(this.floorNumber, 10),
          buildName: this.floorBuildName,
          buildShortName: this.floorBuildShortName,
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

    deleteFloorOf(build: String, floor: string) {
      axios.delete(`${this.floorsEndpoint}/${build}/${floor}`).then(() => {
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

    closeSnackbar() {
      this.snackbar = false;
      this.item = "";
      this.snackbarAction = "";
    },
  },
});

export default floorsMixin;
