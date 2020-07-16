<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>New floor</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="close">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <ValidationObserver ref="observer" v-slot="{ invalid }">
        <v-card-text>
          <v-form ref="form">
            <ValidationProvider v-slot="{ errors }" name="Number of floor" rules="required">
              <v-text-field
                v-model.number="inputFloor.number"
                :error-messages="errors"
                type="number"
                label="Number"
                required
                color="orange accent-2"
              ></v-text-field>
            </ValidationProvider>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="orange darken-1" :disabled="invalid" @click="submit">Add</v-btn>
        </v-card-actions>
      </ValidationObserver>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import { Floor } from "@/interfaces";

import { ValidationObserver, ValidationProvider, extend } from "vee-validate";
import { required } from "vee-validate/dist/rules";

extend("required", {
  ...required,
  message: "{_field_} is required"
});

export default defineComponent({
  props: {
    form: { type: Boolean, required: true },

    floor: { type: Object as () => Floor, required: true }
  },

  components: { ValidationObserver, ValidationProvider },

  setup(props, { emit }) {
    const inputFloor = ref(props.floor);

    watch(
      () => props.floor.number,
      (val: number) => {
        inputFloor.value.number = val;
      }
    );

    const submit = () => {
      emit("submit", inputFloor.value);
    };
    const close = () => emit("close");

    return {
      inputFloor,

      submit,
      close,

      mdiClose
    };
  }
});
</script>