import Vue from "vue";
import VueCompositionAPI from "@vue/composition-api";

import vuetify from "@/plugins/vuetify";
import router from "@/router";
import store from "@/store";

import App from "@/App.vue";

import DefaultLayout from "@/views/layouts/DefaultLayout.vue";
import EmptyLayout from "@/views/layouts/EmptyLayout.vue";

Vue.config.productionTip = false;

Vue.use(VueCompositionAPI);

Vue.component("default-layout", DefaultLayout);
Vue.component("empty-layout", EmptyLayout);

new Vue({
  vuetify,
  router,
  store,

  render: (h) => h(App),
}).$mount("#app");
