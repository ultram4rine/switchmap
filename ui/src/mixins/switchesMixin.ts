import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Switch } from "../interfaces";

const switchesMixin = Vue.extend({
  data() {
    return {
      snackbar: false,

      item: "",

      switches: new Array<Switch>(),
      switchesEndpoint: `${config.apiURL}/switches`,

      switchForm: false,
      addSwitchEndpoint: `${config.apiURL}/switch`,

      switchName: "",
      switchIPResolveMethod: "Direct",
      switchIP: "",
      switchMAC: "",
      switchSNMPCommunityType: "Public",
      switchSNMPCommunity: "",
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
          ipResolveMethod: this.switchIPResolveMethod,
          ip: this.switchIP,
          mac: this.switchMAC,
          snmpCommunityType: this.switchSNMPCommunityType,
          snmpCommunity: this.switchSNMPCommunity,
          build: build,
          floor: floor,
        })
        .then(() => {
          this.switchForm = false;

          this.getAllSwitches();

          this.switchName = "";
          this.switchIPResolveMethod = "Direct";
          this.switchIP = "";
          this.switchMAC = "";
          this.switchSNMPCommunityType = "Public";
          this.switchSNMPCommunity = "";
          this.switchBuild = "";
          this.switchFloor = "";

          this.action = "Add";
        })
        .catch((err) => console.log(err));
    },

    closeSwitchForm() {
      this.switchForm = false;

      this.switchName = "";
      this.switchIPResolveMethod = "Direct";
      this.switchIP = "";
      this.switchMAC = "";
      this.switchSNMPCommunityType = "Public";
      this.switchSNMPCommunity = "";

      this.action = "Add";
    },
  },
});

export default switchesMixin;
