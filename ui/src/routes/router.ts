import Vue from "vue";
import Router, { Route, Location } from "vue-router";

import store from "../store/store";

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";
import Build from "../components/Build.vue";
import Floor from "../components/Floor.vue";
import Switches from "../components/Switches.vue";
import Vis from "../components/Vis.vue";

Vue.use(Router);

const router = new Router({
  routes: [
    {
      path: "/login",
      name: "login",
      component: Login,
      meta: { skipIfAuth: true, layout: "empty" },
    },
    {
      path: "/",
      name: "root",
      redirect: "/builds",
      meta: { requiresAuth: true },
    },
    {
      path: "/builds",
      name: "home",
      component: Home,
      meta: { requiresAuth: false, layout: "default" },
    },
    {
      path: "/builds/:addr",
      name: "build",
      component: Build,
      meta: { requiresAuth: false, layout: "default" },
    },
    {
      path: "/builds/:addr/:floor",
      name: "floor",
      component: Floor,
      meta: { requiresAuth: false, layout: "default" },
    },
    {
      path: "/switches",
      name: "switches",
      component: Switches,
      meta: { requiresAuth: false, layout: "default" },
    },
    {
      path: "/vis",
      name: "visualization",
      component: Vis,
      meta: { requiresAuth: false, layout: "default" },
    },
  ],
});

router.beforeEach(
  (
    to: Route,
    _from: Route,
    next: (
      to?: string | false | void | Location | ((vm: Vue) => any) | undefined
    ) => void
  ) => {
    if (to.matched.some((record) => record.meta.requiresAuth)) {
      if (!store.getters.isAuthenticated) {
        next({
          path: "/login",
          query: { redirect: to.fullPath },
        });
      } else {
        next();
      }
    } else if (to.matched.some((record) => record.meta.skipIfAuth)) {
      if (store.getters.isAuthenticated) {
        next({ path: "/builds" });
      } else {
        next();
      }
    } else {
      next();
    }
  }
);

export default router;
