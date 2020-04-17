import Vue from "vue";
import Router from "vue-router";

import Home from "./components/Home.vue";

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
    {
      path: "/builds/:addr",
      name: "build",
      component: null,
    },
  ],
});
