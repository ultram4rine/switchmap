import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Switch } from "../interfaces";

const switchesMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      item: "",
      snackbarAction: "",

      switches: new Array<Switch>(),
      switchesEndpoint: `${config.apiURL}/switches`,

      switchForm: false,
      addSwitchEndpoint: `${config.apiURL}/switch`,

      switchName: "",
      switchMAC: "",
      switchSNMPCommunity: "Public",
      switchIPResolveMethod: "Direct",
      switchIP: "",
      switchBuild: "",
      switchFloor: "",

      action: "Add",
    };
  },

  methods: {
    getAllSwitches() {
      axios
        .get<Switch, AxiosResponse<Switch[]>>(this.switchesEndpoint)
        .then((resp) => (this.switches = resp.data))
        .catch((err) => console.log(err));
    },

    addSwitch(build: string, floor: string) {
      if (build === "") {
        build = this.switchBuild;
      }
      if (floor === "") {
        floor = this.switchFloor;
      }

      axios
        .post(this.addSwitchEndpoint, {
          name: this.switchName,
          mac: this.switchMAC,
          snmpCommunity: this.switchSNMPCommunity,
          ipResolveMethod: this.switchIPResolveMethod,
          ip: this.switchIP,
          build: build,
          floor: floor,
        })
        .then(() => {
          this.switchForm = false;

          this.getAllSwitches();

          this.switchName = "";
          this.switchMAC = "";
          this.switchSNMPCommunity = "Public";
          this.switchIPResolveMethod = "Direct";
          this.switchIP = "";
          this.switchBuild = "";
          this.switchFloor = "";

          this.action = "Add";
        })
        .catch((err) => console.log(err));
    },

    closeSwitchForm() {
      this.switchForm = false;

      this.switchName = "";
      this.switchMAC = "";
      this.switchSNMPCommunity = "Public";
      this.switchIPResolveMethod = "Direct";
      this.switchIP = "";
      this.switchBuild = "";
      this.switchFloor = "";

      this.action = "Add";
    },

    updateSnackbar(snackbar: boolean) {
      this.snackbar = snackbar;
      this.item = "";
      this.snackbarAction = "";
    },
  },
});

export default switchesMixin;
