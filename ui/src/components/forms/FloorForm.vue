<template>
  <form-wrap :form="form" :title="title" @close="close">
    <ValidationObserver ref="observer" v-slot="{ invalid }">
      <v-form ref="form" @submit.prevent="submit">
        <v-card-text>
          <ValidationProvider
            v-slot="{ errors }"
            name="Number of floor"
            rules="required"
          >
            <v-text-field
              v-model="number"
              :error-messages="errors"
              type="number"
              label="Number"
              required
              color="orange accent-2"
            ></v-text-field>
          </ValidationProvider>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn type="submit" color="orange darken-1" :disabled="invalid">
            Add
          </v-btn>
        </v-card-actions>
      </v-form>
    </ValidationObserver>
  </form-wrap>
</template>

<script lang="ts">
import { defineComponent, ref, watch, PropType } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

import FormWrap from "@/components/wrappers/FormWrap.vue";

import { FloorRequest } from "@/interfaces/floor";

extend("required", {
  ...required,
  message: "{_field_} is required",
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },

    floor: { type: Object as PropType<FloorRequest>, required: true },
  },

  components: { FormWrap, ValidationObserver, ValidationProvider },

  setup(props, { emit }) {
    const number = ref(props.floor.number);

    watch(
      () => props.floor.number,
      (val: number) => {
        number.value = val;
      }
    );

    const submit = () => {
      emit("submit", number.value);
      number.value = 0;
    };
    const close = () => {
      number.value = 0;
      emit("close");
    };

    return {
      number,

      submit,
      close,

      mdiClose,
    };
  },
});
</script>
