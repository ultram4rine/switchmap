import { ref, Ref } from "@vue/composition-api";

export const useDeleteConfirmation = (): {
  deleteConfirmation: Ref<boolean>;
  deleteItemName: Ref<string>;
  confirm: (
    deleteClosure: () => Promise<void>,
    callbackOk: () => void,
    callbackErr: () => void
  ) => Promise<void>;
  cancel: (callback: () => void) => void;
} => {
  const deleteConfirmation = ref(false);
  const deleteItemName = ref("");

  const clean = () => {
    deleteConfirmation.value = false;
    deleteItemName.value = "";
  };

  const confirm = async (
    deleteClosure: () => Promise<void>,
    callbackOk: () => void,
    callbackErr: () => void
  ) => {
    try {
      await deleteClosure();
      callbackOk();
      clean();
    } catch (err: unknown) {
      callbackErr();
    }
  };

  const cancel = (callback: () => void): void => {
    clean();
    callback();
  };

  return {
    deleteConfirmation,
    deleteItemName,

    confirm,
    cancel,
  };
};
