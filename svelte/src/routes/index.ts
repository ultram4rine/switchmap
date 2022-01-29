import DefaultLayout from "../views/layouts/DefaultLayout.svelte";

import Login from "../views/Login.svelte";
import Builds from "../views/Builds.svelte";
import Floors from "../views/Floors.svelte";
import Switches from "../views/Switches.svelte";

const isLoggedIn = () => {
  return true;
};

const routes = [
  {
    name: "/",
    redirectTo: "builds",
  },
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
    name: "builds/:shortName",
    layout: DefaultLayout,
    component: Floors,
  },
  {
    name: "switches",
    layout: DefaultLayout,
    component: Switches,
  },
];

export { routes };
