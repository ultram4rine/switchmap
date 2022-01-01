import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

import store from "@/store";

const Login = () => import("@/views/Login.vue");
const Builds = () =>
  import(/* webpackChunkName: "builds" */ "@/views/Builds.vue");
const Floors = () =>
  import(/* webpackChunkName: "builds" */ "@/views/Floors.vue");
const Floor = () =>
  import(/* webpackChunkName: "builds" */ "@/views/Floor.vue");
const Switches = () => import("@/views/Switches.vue");
const Vis = () => import("@/views/Vis.vue");

const routes: Array<RouteRecordRaw> = [
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

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach((to, _from, next) => {
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
});

export default router;
