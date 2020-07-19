import { ref } from "@vue/composition-api";

export default function () {
  const snackbar = ref(false);
  const item = ref("");
  const action = ref("");

  const updateSnackbar = (val: boolean) => {
    snackbar.value = val;
    item.value = "";
    action.value = "";
  };

  return {
    snackbar,
    item,
    action,

    updateSnackbar,
  };
}
