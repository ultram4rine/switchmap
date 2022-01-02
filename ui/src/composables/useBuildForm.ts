import { ref, Ref } from "@vue/composition-api";

import { BuildRequest } from "@/interfaces/build";

import { addBuild, editBuild } from "@/api/builds";

export const useBuildForm = (): {
  form: Ref<boolean>;
  formAction: Ref<"" | "Add" | "Edit">;
  build: Ref<BuildRequest>;
  openForm: (action: "Add" | "Edit", b?: BuildRequest) => void;
  submitForm: (b: BuildRequest, action: "Add" | "Edit") => Promise<void>;
  closeForm: () => void;
} => {
  const form = ref(false);
  const formAction: Ref<"" | "Add" | "Edit"> = ref("");

  const build: Ref<BuildRequest> = ref({} as BuildRequest);
  const oldShortName = ref("");

  const openForm = (action: "Add" | "Edit", b?: BuildRequest): void => {
    if (b) {
      oldShortName.value = b.shortName;
      build.value = b;
    } else {
      build.value = {} as BuildRequest;
    }

    formAction.value = action;
    form.value = true;
  };

  const submitForm = async (
    b: BuildRequest,
    action: "Add" | "Edit"
  ): Promise<void> => {
    switch (action) {
      case "Add":
        await addBuild(b);
        closeForm();
        break;
      case "Edit":
        await editBuild(b, oldShortName.value);
        closeForm();
        break;
      default:
        break;
    }
  };

  const closeForm = (): void => {
    form.value = false;
    build.value = {} as BuildRequest;
    oldShortName.value = "";
    formAction.value = "";
  };

  return {
    form,
    formAction,

    build,

    openForm,
    submitForm,
    closeForm,
  };
};
