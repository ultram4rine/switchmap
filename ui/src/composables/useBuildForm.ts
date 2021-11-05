import { ref, Ref } from "@vue/composition-api";

import { BuildRequest } from "@/types/build";

const useBuildForm = (): {
  form: Ref<boolean>;
  formAction: Ref<"" | "Add" | "Edit">;
  build: Ref<BuildRequest>;
  oldBuildShortName: Ref<string>;
  openBuildForm: (action: "Add" | "Edit", b?: BuildRequest) => void;
  closeBuildForm: () => void;
} => {
  const form = ref(false);
  const formAction: Ref<"" | "Add" | "Edit"> = ref("");

  const build: Ref<BuildRequest> = ref({} as BuildRequest);
  const oldBuildShortName = ref("");

  const openBuildForm = (action: "Add" | "Edit", b?: BuildRequest): void => {
    if (b) {
      oldBuildShortName.value = b.shortName;
      build.value = b;
    } else {
      build.value = {} as BuildRequest;
    }

    formAction.value = action;
    form.value = true;
  };

  const closeBuildForm = (): void => {
    build.value = {} as BuildRequest;
    oldBuildShortName.value = "";
    formAction.value = "";
    form.value = false;
  };

  return {
    form,
    formAction,

    build,
    oldBuildShortName,

    openBuildForm,
    closeBuildForm,
  };
};

export default useBuildForm;
