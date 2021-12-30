<template>
  <form-wrap :form="form" :title="title" @close="close">
    <ValidationObserver ref="observer" v-slot="{ invalid }">
      <v-form ref="form" @submit.prevent="submit">
        <v-card-text>
          <ValidationProvider v-slot="{ errors }" name="Name" rules="required">
            <v-text-field
              v-model="b.name"
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
              v-model="b.shortName"
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
  </form-wrap>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, PropType } from "vue";
import { mdiClose } from "@mdi/js";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

import FormWrap from "@/components/wrappers/FormWrap.vue";

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

  components: { FormWrap, ValidationObserver, ValidationProvider },

  setup(props, { emit }) {
    const title = computed(() => {
      return props.action === "Add" ? "New build" : "Change build";
    });

    const b = ref({
      name: props.build.name,
      shortName: props.build.shortName,
    } as BuildRequest);
    watch(
      () => props.build,
      (val) => {
        b.value.name = val.name;
        b.value.shortName = val.shortName;
      }
    );

    const submit = () => {
      emit("submit", b.value, props.action);
      b.value = {} as BuildRequest;
    };
    const close = () => {
      b.value = {} as BuildRequest;
      emit("close");
    };

    return {
      title,

      b,

      submit,
      close,

      mdiClose,
    };
  },
});
</script>
