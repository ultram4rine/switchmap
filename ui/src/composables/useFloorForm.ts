import { ref, Ref } from "@vue/composition-api";

import { FloorRequest } from "@/types/floor";

import { addFloor } from "@/api/floors";
import { getBuild } from "@/api/builds";

const useFloorForm = (): {
  form: Ref<boolean>;
  floor: Ref<FloorRequest>;
  buildShortName: Ref<string>;
  openForm: (shortName?: string) => void;
  submitForm: (number: number, callback: () => void) => void;
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

  const submitForm = (number: number, callback: () => void): void => {
    try {
      getBuild(buildShortName.value).then((b) => {
        addFloor({
          number,
          buildName: b.name,
          buildShortName: buildShortName.value,
        } as FloorRequest).then(() => callback());
        closeForm();
      });
    } catch (error: any) {
      console.log(error);
    }
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
