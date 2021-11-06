import { ref, Ref } from "@vue/composition-api";

const useDeleteConfirmation = (): {
  deleteConfirmation: Ref<boolean>;
  deleteItemName: Ref<string>;
  cancel: (callback: () => void) => void;
} => {
  const deleteConfirmation = ref(false);
  const deleteItemName = ref("");

  const cancel = (callback: () => void): void => {
    deleteConfirmation.value = false;
    deleteItemName.value = "";
    callback();
  };

  return {
    deleteConfirmation,
    deleteItemName,

    cancel,
  };
};

export default useDeleteConfirmation;
