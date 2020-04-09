import Vue from "vue";
import Router from "vue-router";
import vuetify from "./plugins/vuetify";

import App from "./App.vue";
import Home from "./components/Home.vue";

Vue.use(Router);

const router = new Router({
  routes: [
    {
      path: "/",
      name: "home",
      component: Home
    },
    {
      path: "/map/:id",
      name: "build",
      component: null,
      props: true
    }
  ]
});

new Vue({
  vuetify,
  router,
  el: "#app",
  components: { App },
  template: "<App/>"
});
