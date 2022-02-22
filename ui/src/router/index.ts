import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

import { useAuth } from "@/store/auth";

const LoginPage = () => import("@/views/LoginPage.vue");
const BuildsPage = () =>
  import(/* webpackChunkName: "builds" */ "@/views/BuildsPage.vue");
const FloorsPage = () =>
  import(/* webpackChunkName: "builds" */ "@/views/FloorsPage.vue");
const FloorPage = () =>
  import(/* webpackChunkName: "builds" */ "@/views/FloorPage.vue");
const SwitchesPage = () => import("@/views/SwitchesPage.vue");
const VisPage = () => import("@/views/VisPage.vue");

const defaultLayout = "default-layout";
const emptyLayout = "empty-layout";

const routes: Array<RouteRecordRaw> = [
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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to, _from, next) => {
  const authStore = useAuth();
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!authStore.getLoggedIn) {
      next({
        path: "/login",
        query: { redirect: to.fullPath },
      });
    } else {
      next();
    }
  } else if (to.matched.some((record) => record.meta.skipIfAuth)) {
    if (authStore.getLoggedIn) {
      next({ path: "/builds" });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;
