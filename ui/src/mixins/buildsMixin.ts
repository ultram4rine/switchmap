import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Build } from "../interfaces";

const buildsMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      timeout: 3000,

      item: "",

      builds: new Array<Build>(),
      buildsEndpoint: `${config.apiURL}/builds`,

      addBuildForm: false,
      addBuildEndpoint: `${config.apiURL}/build`,
      buildName: "",
      buildAddr: "",
    };
  },

  methods: {
    getAllBuilds() {
      axios
        .get<Build, AxiosResponse<Build[]>>(this.buildsEndpoint)
        .then((resp) => (this.builds = resp.data))
        .catch((err) => console.log(err));
    },

    addBuildMixin() {
      axios
        .post(this.addBuildEndpoint, {
          name: this.buildName,
          addr: this.buildAddr,
        })
        .then(() => {
          this.addBuildForm = false;

          this.getAllBuilds();

          this.item = this.buildName;
          this.snackbar = true;

          this.buildName = "";
          this.buildAddr = "";
        })
        .catch((err) => console.log(err));
    },
  },
});

export default buildsMixin;
