import Vue from "vue";
import VueCompositionAPI from "@vue/composition-api";

import vuetify from "@/plugins/vuetify";
import router from "@/routes";
import store from "@/store";

import App from "@/App.vue";

import Layout from "@/components/layouts/Layout.vue";
import Empty from "@/components/layouts/Empty.vue";

Vue.config.productionTip = false;

Vue.use(VueCompositionAPI);

Vue.component("default", Layout);
Vue.component("empty", Empty);

new Vue({
  vuetify,
  router,
  store,

  render: (h) => h(App),
}).$mount("#app");
