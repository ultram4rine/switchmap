import Vue from "vue";
import VueRouter, { Route, Location, RouteConfig } from "vue-router";

import store from "@/store";

import Login from "@/views/Login.vue";
import Builds from "@/views/Builds.vue";
import Floors from "@/views/Floors.vue";
import Floor from "@/views/Floor.vue";
import Switches from "@/views/Switches.vue";
import Vis from "@/views/Vis.vue";

Vue.use(VueRouter);

const routes: Array<RouteConfig> = [
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
    component: Builds,
    meta: { requiresAuth: true, layout: "default" },
  },
  {
    path: "/builds/:shortName",
    name: "build",
    component: Floors,
    props: true,
    meta: { requiresAuth: true, layout: "default" },
  },
  {
    path: "/builds/:shortName/f:floor",
    name: "floor",
    component: Floor,
    props: true,
    meta: { requiresAuth: true, layout: "default" },
  },
  {
    path: "/switches",
    name: "switches",
    component: Switches,
    meta: { requiresAuth: true, layout: "default" },
  },
  {
    path: "/vis",
    name: "visualization",
    component: Vis,
    meta: { requiresAuth: true, layout: "default" },
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach(
  (
    to: Route,
    _from: Route,
    next: (
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      to?: string | false | void | Location | ((vm: Vue) => any) | undefined
    ) => void
  ) => {
    if (to.matched.some((record) => record.meta.requiresAuth)) {
      if (!store.getters.getLoggedIn) {
        next({
          path: "/login",
          query: { redirect: to.fullPath },
        });
      } else {
        next();
      }
    } else if (to.matched.some((record) => record.meta.skipIfAuth)) {
      if (store.getters.getLoggedIn) {
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
