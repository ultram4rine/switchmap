import Vue from "vue";
import Vuex from "vuex";

import { state, getters, actions, mutations } from "./modules/auth";

Vue.use(Vuex);

const store = new Vuex.Store({
  state: state,
  getters: getters,
  actions: actions,
  mutations: mutations,
  strict: process.env.NODE_ENV !== "production",
});

export default store;
