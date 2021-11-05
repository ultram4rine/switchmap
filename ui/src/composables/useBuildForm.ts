import { ref, Ref } from "@vue/composition-api";

import { BuildRequest } from "@/types/build";

import { addBuild, editBuild } from "@/api/builds";

const useBuildForm = (): {
  form: Ref<boolean>;
  formAction: Ref<"" | "Add" | "Edit">;
  build: Ref<BuildRequest>;
  oldBuildShortName: Ref<string>;
  openForm: (action: "Add" | "Edit", b?: BuildRequest) => void;
  submitForm: (
    name: string,
    shortName: string,
    action: "Add" | "Edit",
    callback: () => void
  ) => void;
  closeForm: () => void;
} => {
  const form = ref(false);
  const formAction: Ref<"" | "Add" | "Edit"> = ref("");

  const build: Ref<BuildRequest> = ref({} as BuildRequest);
  const oldBuildShortName = ref("");

  const openForm = (action: "Add" | "Edit", b?: BuildRequest): void => {
    if (b) {
      oldBuildShortName.value = b.shortName;
      build.value = b;
    } else {
      build.value = {} as BuildRequest;
    }

    formAction.value = action;
    form.value = true;
  };

  const submitForm = (
    name: string,
    shortName: string,
    action: "Add" | "Edit",
    callback: () => void
  ): void => {
    try {
      switch (action) {
        case "Add":
          addBuild({ name, shortName } as BuildRequest).then(() => callback());
          closeForm();
          break;
        case "Edit":
          editBuild(
            { name, shortName } as BuildRequest,
            oldBuildShortName.value
          ).then(() => callback());
          closeForm();
          break;
        default:
          break;
      }
    } catch (err: any) {
      console.log(err);
    }
  };

  const closeForm = (): void => {
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

    openForm,
    submitForm,
    closeForm,
  };
};

export default useBuildForm;
