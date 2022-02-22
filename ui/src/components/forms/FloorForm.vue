<template>
  <form-wrap :form="form" title="New floor" @close="close">
    <v-form ref="form" @submit.prevent="submit">
      <v-card-text>
        <v-text-field
          v-model="f.number"
          :error-messages="errors"
          type="number"
          label="Number"
          required
          color="orange accent-2"
        ></v-text-field>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          type="submit"
          color="orange darken-1"
          :disabled="errors || isSubmitting"
        >
          Add
        </v-btn>
      </v-card-actions>
    </v-form>
  </form-wrap>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "vue";
import type { PropType } from "vue";

import { useForm } from "vee-validate";

import FormWrap from "@/components/wrappers/FormWrap.vue";

import type { FloorRequest } from "@/interfaces/floor";

import { FloorSchema } from "@/validations/FloorSchema";

import { mdiClose } from "@mdi/js";

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },

    floor: { type: Object as PropType<FloorRequest>, required: true },
  },

  components: { FormWrap },

  setup(props, { emit }) {
    const f = ref({ number: props.floor.number } as FloorRequest);
    watch(
      () => props.floor,
      (val) => {
        f.value.number = val.number;
      }
    );

    const submit = () => {
      emit("submit", f.value);
      f.value = {} as FloorRequest;
    };
    const close = () => {
      f.value = {} as FloorRequest;
      emit("close");
    };

    const { errors, isSubmitting } = useForm<{ number: number }>({
      initialValues: f.value,
      validationSchema: FloorSchema,
    });

    return {
      f,

      submit,
      close,

      errors,
      isSubmitting,

      mdiClose,
    };
  },
});
</script>
