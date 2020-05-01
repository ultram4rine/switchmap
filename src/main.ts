import Vue from "vue";

import vuetify from "./plugins/vuetify";
import router from "./routes/router";
import store from "./store/store";

import App from "./App.vue";

import Layout from "./components/Layout.vue";

Vue.config.productionTip = false;

Vue.component("layout", Layout);

new Vue({
  vuetify,
  router,
  store,

  el: "#app",
  components: { App },
  template: "<App/>",
});
