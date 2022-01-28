import { ref, Ref } from "@vue/composition-api";

import { FloorRequest } from "@/interfaces/floor";

import { addFloor } from "@/api/floors";

export const useFloorForm = (): {
  form: Ref<boolean>;
  floor: Ref<FloorRequest>;
  buildShortName: Ref<string>;
  openForm: (shortName?: string) => void;
  submitForm: (f: FloorRequest) => Promise<void>;
  closeForm: () => void;
} => {
  const form = ref(false);

  const floor: Ref<FloorRequest> = ref({} as FloorRequest);
  const buildShortName = ref("");

  const openForm = (shortName?: string): void => {
    if (shortName) {
      buildShortName.value = shortName;
    }

    form.value = true;
  };

  const submitForm = async (f: FloorRequest): Promise<void> => {
    f.buildShortName = buildShortName.value;
    await addFloor(f);
    closeForm();
  };

  const closeForm = (): void => {
    form.value = false;
    buildShortName.value = "";
    floor.value = {} as FloorRequest;
  };

  return {
    form,

    floor,
    buildShortName,

    openForm,
    submitForm,
    closeForm,
  };
};
