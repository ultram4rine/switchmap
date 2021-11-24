<template>
  <v-dialog :value="form" persistent max-width="500px" width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <ValidationObserver ref="observer" v-slot="{ invalid }">
        <v-form ref="form" @submit.prevent="submit">
          <v-card-text>
            <ValidationProvider
              v-slot="{ errors }"
              name="Name"
              rules="required"
            >
              <v-text-field
                v-model="name"
                :error-messages="errors"
                label="Name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>

            <ValidationProvider
              v-slot="{ errors }"
              name="Short name"
              rules="required"
            >
              <v-text-field
                v-model="shortName"
                :error-messages="errors"
                label="Short name"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn type="submit" color="orange darken-1" :disabled="invalid">
              {{ action }}
            </v-btn>
          </v-card-actions>
        </v-form>
      </ValidationObserver>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  computed,
  watch,
  PropType,
} from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

import { BuildRequest } from "@/interfaces/build";

extend("required", {
  ...required,
  message: "{_field_} is required",
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true, enum: ["Add", "Edit"] },

    build: { type: Object as PropType<BuildRequest>, required: true },
  },

  components: { ValidationObserver, ValidationProvider },

  setup(props, { emit }) {
    const title = computed(() => {
      return props.action === "Add" ? "New build" : "Change build";
    });

    const name = ref(props.build.name);
    const shortName = ref(props.build.shortName);

    watch(
      () => props.build.name,
      (val) => {
        name.value = val;
      }
    );
    watch(
      () => props.build.shortName,
      (val) => {
        shortName.value = val;
      }
    );

    const submit = () => {
      emit("submit", name.value, shortName.value, props.action);
      name.value = "";
      shortName.value = "";
    };
    const close = () => {
      name.value = "";
      shortName.value = "";
      emit("close");
    };

    return {
      title,

      name,
      shortName,

      submit,
      close,

      mdiClose,
    };
  },
});
</script>
