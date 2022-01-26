import { createApp } from "vue";
import { createPinia } from "pinia";

import router from "@/router";
import vuetify from "@/plugins/vuetify";
import { loadFonts } from "./plugins/webfontloader";

loadFonts();

import App from "@/App.vue";

import DefaultLayout from "@/views/layouts/DefaultLayout.vue";
import EmptyLayout from "@/views/layouts/EmptyLayout.vue";

createApp(App)
  .component("default-layout", DefaultLayout)
  .component("empty-layout", EmptyLayout)
  .use(router)
  .use(createPinia())
  .use(vuetify)
  .mount("#app");
