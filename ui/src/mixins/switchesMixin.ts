import Vue from "vue";
import axios, { AxiosResponse } from "axios";

import { config } from "../config";
import { Switch } from "../interfaces";

const switchesMixin = Vue.extend({
  data() {
    return {
      snackbar: false,
      timeout: 3000,

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

    addSwitch() {
      this.switchForm = false;
      console.log(
        this.switchName,
        this.switchIPResolveMethod,
        this.switchIP,
        this.switchMAC,
        this.switchSNMPCommunityType,
        this.switchSNMPCommunity
      );
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

    updateSwitchName(name: string) {
      this.switchName = name;
    },
    updateSwitchIPResolveMethod(method: string) {
      this.switchIPResolveMethod = method;
    },
    updateSwitchIP(ip: string) {
      this.switchIP = ip;
    },
    updateSwitchMAC(mac: string) {
      this.switchMAC = mac;
    },
    updateSwitchSNMPCommunityType(type: string) {
      this.switchSNMPCommunityType = type;
    },
    updateSwitchSNMPCommunity(community: string) {
      this.switchSNMPCommunity = community;
    },
  },
});

export default switchesMixin;
