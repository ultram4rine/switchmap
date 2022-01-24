import DefaultLayout from "../views/layouts/DefaultLayout.svelte";

import Login from "../views/Login.svelte";

const isLoggedIn = () => {
  return true;
};

const routes = [
  {
    name: "login",
    component: Login,
  },
];

export { routes };
