import { createApp } from "vue";

import router from "@/router";
import store from "@/store";
import vuetify from "@/plugins/vuetify";

import App from "@/App.vue";

import Layout from "@/views/layouts/Layout.vue";
import Empty from "@/views/layouts/Empty.vue";

createApp(App)
  .component("default", Layout)
  .component("empty", Empty)
  .use(router)
  .use(store)
  .use(vuetify)
  .mount("#app");
