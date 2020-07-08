<template>
  <v-dialog :value="form" persistent max-width="500px">
    <v-card dark>
      <v-toolbar>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="$emit('close')">
          <v-icon>{{ mdiClose }}</v-icon>
        </v-btn>
      </v-toolbar>

      <v-card-text>
        <v-form ref="form">
          <v-text-field v-model="inputName.input" label="Name" color="orange accent-2" required></v-text-field>
          <v-text-field
            v-model="inputShortName.input"
            label="Short Name"
            color="orange accent-2"
            required
          ></v-text-field>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="orange darken-1" @click="$emit('submit')">{{ action }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent, computed } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

import useInputValidator from "@/helpers/useInputValidator";

export default defineComponent({
  name: "BuildForm",

  props: {
    form: { type: Boolean, required: true },
    action: { type: String, required: true },
    name: { type: String, required: true },
    shortName: { type: String, required: true }
  },

  setup(props, { emit }) {
    const title = computed(() => {
      if (props.action == "Add") return "New build";
      else if (props.action == "Change") return "Change build";
    });

    const inputName = useInputValidator(props.name, [], (name: string) =>
      emit("input", name)
    );
    const inputShortName = useInputValidator(
      props.shortName,
      [],
      (shortName: string) => emit("input", shortName)
    );

    return {
      title,

      inputName,
      inputShortName,

      mdiClose
    };
  }
});
</script>