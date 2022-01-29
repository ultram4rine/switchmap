import DefaultLayout from "../views/layouts/DefaultLayout.svelte";

import Login from "../views/Login.svelte";
import Builds from "../views/Builds.svelte";
import Switches from "../views/Switches.svelte";

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
  {
    name: "switches",
    layout: DefaultLayout,
    component: Switches,
  },
];

export { routes };
