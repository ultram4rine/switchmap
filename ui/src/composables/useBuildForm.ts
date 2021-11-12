import { ref, Ref } from "@vue/composition-api";

import { BuildRequest } from "@/types/build";

import { addBuild, editBuild } from "@/api/builds";

const useBuildForm = (): {
  form: Ref<boolean>;
  formAction: Ref<"" | "Add" | "Edit">;
  build: Ref<BuildRequest>;
  openForm: (action: "Add" | "Edit", b?: BuildRequest) => void;
  submitForm: (
    name: string,
    shortName: string,
    action: "Add" | "Edit"
  ) => Promise<void>;
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
    name: string,
    shortName: string,
    action: "Add" | "Edit"
  ): Promise<void> => {
    switch (action) {
      case "Add":
        await addBuild({ name, shortName } as BuildRequest);
        closeForm();
        break;
      case "Edit":
        await editBuild(
          { name, shortName } as BuildRequest,
          oldShortName.value
        );
        closeForm();
        break;
      default:
        break;
    }
  };

  const closeForm = (): void => {
    build.value = {} as BuildRequest;
    oldShortName.value = "";
    formAction.value = "";
    form.value = false;
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

export default useBuildForm;
