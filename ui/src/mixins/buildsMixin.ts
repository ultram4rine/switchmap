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
      buildEndpoint: `${config.apiURL}/build/`,

      buildForm: false,
      addBuildEndpoint: `${config.apiURL}/build`,

      buildName: "",
      buildAddr: "",

      action: "Add",
    };
  },

  methods: {
    getAllBuilds() {
      axios
        .get<Build, AxiosResponse<Build[]>>(this.buildsEndpoint)
        .then((resp) => (this.builds = resp.data))
        .catch((err) => console.log(err));
    },

    getBuild(buildAddr: string) {
      axios
        .get<Build, AxiosResponse<Build>>(this.buildEndpoint + buildAddr)
        .then((resp) =>
          Vue.set(
            this.builds,
            this.builds.findIndex((b) => b.addr === buildAddr),
            resp.data
          )
        )
        .catch((err) => console.log(err));
    },

    addBuild() {
      axios
        .post(this.addBuildEndpoint, {
          name: this.buildName,
          addr: this.buildAddr,
        })
        .then(() => {
          this.buildForm = false;

          this.getAllBuilds();

          this.item = this.buildName;
          this.snackbar = true;

          this.buildName = "";
          this.buildAddr = "";
        })
        .catch((err) => console.log(err));
    },

    updateBuild(buildAddr: string) {
      axios
        .put(this.buildEndpoint + buildAddr, {
          name: this.buildName,
          addr: this.buildAddr,
        })
        .then(() => {
          this.buildForm = false;

          this.getBuild(buildAddr);

          this.buildName = "";
          this.buildAddr = "";
        });
    },

    deleteBuild(buildAddr: string) {
      axios.delete(this.buildEndpoint + buildAddr).then(() => {
        this.getAllBuilds();
      });
    },

    closeBuildForm() {
      this.buildForm = false;
      this.buildName = "";
      this.buildAddr = "";

      this.action = "Add";
    },

    updateBuildName(name: string) {
      this.buildName = name;
    },
    updateBuildAddr(addr: string) {
      this.buildAddr = addr;
    },
  },
});

export default buildsMixin;
