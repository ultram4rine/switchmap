import Vue from "vue";
import Router, { Route, Location } from "vue-router";

import store from "../store/store";

import Login from "../components/Login.vue";
import Home from "../components/Home.vue";

Vue.use(Router);

const router = new Router({
  routes: [
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
      meta: { requiresAuth: true, layout: "default" },
    },
    {
      path: "/login",
      name: "login",
      component: Login,
      meta: { layout: "empty" },
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
      console.log(store.getters.isAuthenticated);
      if (!store.getters.isAuthenticated) {
        next({
          path: "/login",
          query: { redirect: to.fullPath },
        });
      } else {
        next();
      }
    } else {
      next();
    }
  }
);

export default router;
