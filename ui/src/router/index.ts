import Vue from "vue";
import VueRouter, { Route, Location, RouteConfig } from "vue-router";

import store from "@/store";

import LoginPage from "@/views/LoginPage.vue";
import BuildsPage from "@/views/BuildsPage.vue";
import FloorsPage from "@/views/FloorsPage.vue";
import FloorPage from "@/views/FloorPage.vue";
import SwitchesPage from "@/views/SwitchesPage.vue";
import VisPage from "@/views/VisPage.vue";

Vue.use(VueRouter);

const defaultLayout = "default-layout";
const emptyLayout = "empty-layout";

const routes: Array<RouteConfig> = [
  {
    path: "/login",
    name: "login",
    component: LoginPage,
    meta: { skipIfAuth: true, layout: emptyLayout },
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
    component: BuildsPage,
    meta: { requiresAuth: true, layout: defaultLayout },
  },
  {
    path: "/builds/:shortName",
    name: "build",
    component: FloorsPage,
    props: true,
    meta: { requiresAuth: true, layout: defaultLayout },
  },
  {
    path: "/builds/:shortName/f:floor",
    name: "floor",
    component: FloorPage,
    props: true,
    meta: { requiresAuth: true, layout: defaultLayout },
  },
  {
    path: "/switches",
    name: "switches",
    component: SwitchesPage,
    meta: { requiresAuth: true, layout: defaultLayout },
  },
  {
    path: "/vis",
    name: "visualization",
    component: VisPage,
    meta: { requiresAuth: true, layout: defaultLayout },
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
