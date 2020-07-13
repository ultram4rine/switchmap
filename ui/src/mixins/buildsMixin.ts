import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "@/config";
import { Build } from "@/interfaces";

const buildsMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      item: "",
      snackbarAction: "",

      builds: new Array<Build>(),
      buildsEndpoint: `${config.apiURL}/builds`,
      buildEndpoint: (build: string) => {
        return `${config.apiURL}/builds/${build}`;
      },

      buildForm: false,

      buildName: "",
      buildShortName: "",

      action: "Add",

      buildForUpdate: "",

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

    getBuild(build: string) {
      axios
        .get<Build, AxiosResponse<Build>>(this.buildEndpoint(build))
        .then((resp) =>
          Vue.set(
            this.builds,
            this.builds.findIndex((b) => b.shortName === build),
            resp.data
          )
        )
        .catch((err) => console.log(err));
    },

    addBuild() {
      axios
        .post(this.buildsEndpoint, {
          name: this.buildName,
          shortName: this.buildShortName,
        })
        .then(() => {
          this.buildForm = false;

          this.getAllBuilds();

          this.item = this.buildName;
          this.snackbarAction = "added";
          this.snackbar = true;

          this.buildName = "";
          this.buildShortName = "";
        })
        .catch((err) => console.log(err));
    },

    updateBuild(buildForUpdate: string) {
      axios
        .put(this.buildEndpoint(buildForUpdate), {
          name: this.buildName,
          shortName: this.buildShortName,
        })
        .then(() => {
          this.buildForm = false;

          this.getBuild(this.buildShortName);

          this.item = this.buildName;
          this.snackbarAction = "updated";
          this.snackbar = true;

          this.buildName = "";
          this.buildShortName = "";
        });
    },

    deleteBuild(build: string) {
      axios.delete(this.buildEndpoint(build)).then(() => {
        this.confirmation = !this.confirmation;
        this.getAllBuilds();

        this.item = this.buildName;
        this.snackbarAction = "deleted";
        this.snackbar = true;
      });
    },

    closeBuildForm() {
      this.buildForm = false;
      this.buildName = "";
      this.buildShortName = "";

      this.action = "Add";
    },

    updateSnackbar(snackbar: boolean) {
      this.snackbar = snackbar;
      this.item = "";
      this.snackbarAction = "";
    },
  },
});

export default buildsMixin;
