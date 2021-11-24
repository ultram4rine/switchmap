import { ref, Ref } from "@vue/composition-api";

import { FloorRequest } from "@/interfaces/floor";

import { addFloor } from "@/api/floors";
import { getBuild } from "@/api/builds";

export const useFloorForm = (): {
  form: Ref<boolean>;
  floor: Ref<FloorRequest>;
  buildShortName: Ref<string>;
  openForm: (shortName?: string) => void;
  submitForm: (number: number) => Promise<void>;
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

  const submitForm = async (number: number): Promise<void> => {
    const b = await getBuild(buildShortName.value);
    await addFloor({
      number,
      buildName: b.name,
      buildShortName: buildShortName.value,
    } as FloorRequest);
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
