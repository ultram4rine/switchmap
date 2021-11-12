import { ref, Ref } from "@vue/composition-api";

const useSnackbar = (): {
  snackbar: Ref<boolean>;
  snackbarType: Ref<"success" | "info" | "warning" | "error">;
  text: Ref<string>;
  open: (
    sbType: "success" | "info" | "warning" | "error",
    snackbarText: string
  ) => void;
  close: () => void;
} => {
  const snackbar = ref(false);
  const snackbarType: Ref<"success" | "info" | "warning" | "error"> =
    ref("success");
  const text = ref("");

  const open = (
    sbType: "success" | "info" | "warning" | "error",
    snackbarText: string
  ) => {
    snackbarType.value = sbType;
    text.value = snackbarText;
    snackbar.value = true;
  };

  const close = () => {
    snackbar.value = false;
  };

  return {
    snackbar,
    snackbarType,
    text,

    open,
    close,
  };
};

export default useSnackbar;
