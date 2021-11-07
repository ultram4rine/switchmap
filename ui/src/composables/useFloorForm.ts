import { ref, Ref } from "@vue/composition-api";

import { FloorRequest } from "@/types/floor";

import { addFloor } from "@/api/floors";
import { getBuild } from "@/api/builds";

const useFloorForm = (): {
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
    buildShortName.value = "";
    floor.value = {} as FloorRequest;
    form.value = false;
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

export default useFloorForm;
