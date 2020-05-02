import Vue from "vue";
import axios from "axios";

import vuetify from "./plugins/vuetify";
import router from "./routes/router";
import store from "./store/store";

import App from "./App.vue";

import Layout from "./components/Layout.vue";
import Empty from "./components/Empty.vue";

Vue.config.productionTip = false;

Vue.component("default", Layout);
Vue.component("empty", Empty);

const token = localStorage.getItem("user-token");
if (token) {
  axios.defaults.headers.common["Authorization"] = token;
}

new Vue({
  vuetify,
  router,
  store,

  el: "#app",
  components: { App },
  template: "<App/>",
});
