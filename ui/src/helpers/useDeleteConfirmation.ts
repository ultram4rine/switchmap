import { ref, Ref } from "@vue/composition-api";

const useDeleteConfirmation = (): {
  deleteConfirmation: Ref<boolean>;
  deleteItemName: Ref<string>;
} => {
  const deleteConfirmation = ref(false);
  const deleteItemName = ref("");

  return {
    deleteConfirmation,
    deleteItemName,
  };
};

export default useDeleteConfirmation;
