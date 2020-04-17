import Vue from "vue";
import vuetify from "./plugins/vuetify";
import router from "./routes/router";

import App from "./App.vue";

new Vue({
  vuetify,
  router,
  el: "#app",
  components: { App },
  template: "<App/>",
});
