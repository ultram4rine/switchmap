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
    },

    closeSwitchForm() {
      this.switchForm = false;
    },
  },
});

export default switchesMixin;
