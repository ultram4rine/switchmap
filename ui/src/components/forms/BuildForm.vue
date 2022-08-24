<template>
  <form-wrap :form="form" :title="title" @close="close">
    <v-form ref="form" @submit.prevent="submit">
      <v-card-text>
        <v-text-field
          v-model="b.name"
          :error-messages="errors.name"
          label="Name"
          required
          color="orange accent-2"
        ></v-text-field>

        <v-text-field
          v-model="b.shortName"
          :error-messages="errors.shortName"
          label="Short name"
          required
          color="orange accent-2"
        ></v-text-field>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="close">Close</v-btn>
        <v-btn
          type="submit"
          color="orange darken-1"
          :disabled="!!errors || isSubmitting"
        >
          {{ action }}
        </v-btn>
      </v-card-actions>
    </v-form>
  </form-wrap>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, PropType } from "vue";

import { useForm } from "vee-validate";

import FormWrap from "@/components/wrappers/FormWrap.vue";

import { BuildRequest } from "@/interfaces/build";

import { BuildSchema } from "@/validations/BuildSchema";

import { mdiClose } from "@mdi/js";

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true, enum: ["Add", "Edit"] },

    build: { type: Object as PropType<BuildRequest>, required: true },
  },

  components: { FormWrap },

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

    const { errors, isSubmitting } = useForm<BuildRequest>({
      initialValues: b.value,
      validationSchema: BuildSchema,
    });

    return {
      title,

      b,

      submit,
      close,

      errors,
      isSubmitting,

      mdiClose,
    };
  },
});
</script>
