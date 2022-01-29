import DefaultLayout from "../views/layouts/DefaultLayout.svelte";

import Login from "../views/Login.svelte";
import Builds from "../views/Builds.svelte";

const isLoggedIn = () => {
  return true;
};

const routes = [
  {
    name: "login",
    component: Login,
  },
  {
    name: "builds",
    layout: DefaultLayout,
    component: Builds,
  },
];

export { routes };
