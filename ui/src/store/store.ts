import Vue from "vue";
import Vuex from "vuex";

import auth from "./modules/auth";
import csrf from "./modules/csrf";

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== "production";

const store = new Vuex.Store({
  modules: {
    auth: auth,
    csrf: csrf,
  },
  strict: debug,
});

export default store;
