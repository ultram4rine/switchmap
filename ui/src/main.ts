import { createApp } from "vue";

import router from "@/router";
import store from "@/store";
import vuetify from "@/plugins/vuetify";
import { loadFonts } from "./plugins/webfontloader";

loadFonts();

import App from "@/App.vue";

import DefaultLayout from "@/views/layouts/DefaultLayout.vue";
import EmptyLayout from "@/views/layouts/EmptyLayout.vue";

createApp(App)
  .component("default", DefaultLayout)
  .component("empty", EmptyLayout)
  .use(router)
  .use(store)
  .use(vuetify)
  .mount("#app");
