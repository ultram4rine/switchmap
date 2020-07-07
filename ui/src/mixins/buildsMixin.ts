import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Build } from "../interfaces";

const buildsMixin = Vue.extend({
  data() {
    return {
      snackbar: false,

      item: "",

      builds: new Array<Build>(),
      buildsEndpoint: `${config.apiURL}/builds`,
      buildEndpoint: `${config.apiURL}/build/`,

      buildForm: false,
      addBuildEndpoint: `${config.apiURL}/build`,

      buildName: "",
      buildShortName: "",

      action: "Add",

      confirmation: false,
      buildForDeleteName: "",
      buildForDeleteShortName: "",
    };
  },

  methods: {
    getAllBuilds() {
      axios
        .get<Build, AxiosResponse<Build[]>>(this.buildsEndpoint)
        .then((resp) => (this.builds = resp.data))
        .catch((err) => console.log(err));
    },

    getBuild(buildShortName: string) {
      axios
        .get<Build, AxiosResponse<Build>>(this.buildEndpoint + buildShortName)
        .then((resp) =>
          Vue.set(
            this.builds,
            this.builds.findIndex((b) => b.shortName === buildShortName),
            resp.data
          )
        )
        .catch((err) => console.log(err));
    },

    addBuild() {
      axios
        .post(this.addBuildEndpoint, {
          name: this.buildName,
          shortName: this.buildShortName,
        })
        .then(() => {
          this.buildForm = false;

          this.getAllBuilds();

          this.item = this.buildName;
          this.snackbar = true;

          this.buildName = "";
          this.buildShortName = "";
        })
        .catch((err) => console.log(err));
    },

    updateBuild(buildShortName: string) {
      axios
        .put(this.buildEndpoint + buildShortName, {
          name: this.buildName,
          addr: this.buildShortName,
        })
        .then(() => {
          this.buildForm = false;

          this.getBuild(buildShortName);

          this.buildName = "";
          this.buildShortName = "";
        });
    },

    deleteBuild(buildShortName: string) {
      axios.delete(this.buildEndpoint + buildShortName).then(() => {
        this.confirmation = !this.confirmation;
        this.getAllBuilds();
      });
    },

    closeBuildForm() {
      this.buildForm = false;
      this.buildName = "";
      this.buildShortName = "";

      this.action = "Add";
    },
  },
});

export default buildsMixin;
