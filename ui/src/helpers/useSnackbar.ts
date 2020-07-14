import { ref } from "@vue/composition-api";

export default function () {
  const snackbar = ref("");
  const action = ref("");
  const item = ref("");

  return {
    snackbar,
    action,
    item,
  };
}
