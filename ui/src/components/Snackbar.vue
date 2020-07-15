<template>
  <v-snackbar :value="snackbar" :timeout="timeout" @input="close">
    {{ selfItem }} {{ selfAction }}
    <template v-slot:action="{ attrs }">
      <v-btn fab x-small v-bind="attrs" @click="close">
        <v-icon dark>{{ mdiClose }}</v-icon>
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from "@vue/composition-api";
import { mdiClose } from "@mdi/js";

export default defineComponent({
  props: {
    snackbar: { type: Boolean, required: true },
    item: { type: String, required: true },
    action: { type: String, required: true }
  },

  setup(props, { emit }) {
    const timeout = ref(3000);

    let selfItem = ref(props.item);
    let selfAction = ref(props.action);

    watch(
      () => props.item,
      (value: string) => {
        selfItem.value = value;
      }
    );
    watch(
      () => props.action,
      (value: string) => {
        selfAction.value = value;
      }
    );

    const close = () => {
      emit("update", false);
    };

    return {
      timeout,

      selfItem,
      selfAction,

      close,

      mdiClose
    };
  }
});
</script>