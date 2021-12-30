import { createStore } from "vuex";

import { state, getters, actions, mutations } from "./modules/auth";

const store = createStore({
  state: state,
  getters: getters,
  actions: actions,
  mutations: mutations,
  strict: process.env.NODE_ENV !== "production",
});

export default store;
