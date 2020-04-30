import Vue from "vue";
import Component from "vue-class-component";
import Router from "vue-router";

import Home from "../components/Home.vue";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: "/",
      redirect: "/builds",
    },
    {
      path: "/builds",
      name: "home",
      component: Home,
    },
  ],
});
