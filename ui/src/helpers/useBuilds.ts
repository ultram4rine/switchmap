import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Build } from "@/interfaces";

const buildsEndpoint = `${config.apiURL}/builds`;
const buildEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}`;
};

export default function () {
  const builds: Ref<Build[]> = ref([]);

  const buildForm = ref(false);
  const buildAction = ref("Add");
  const buildName = ref("");
  const buildShortName = ref("");

  const openBuildForm = (action: string, b?: Build) => {
    buildAction.value = action;
    switch (action) {
      case "Add":
        buildName.value = "";
        buildShortName.value = "";
        break;
      case "Change":
        if (b != undefined) {
          buildName.value = b.name;
          buildShortName.value = b.shortName;
        }
        break;
      default:
        break;
    }
    buildForm.value = true;
  };

  const handleSubmitBuild = (name: string, shortName: string) => {
    switch (buildAction.value) {
      case "Add":
        addBuild(name, shortName).then(() =>
          getAllBuilds().then(() => closeBuildForm())
        );
        break;
      case "Change":
        updateBuild(build.value, name, shortName).then(() => {
          getBuild(build.value).then(() => {
            const i = builds.value.findIndex(
              (b) => b.shortName === build.value.shortName
            );
            builds.value[i] = build.value;

            closeBuildForm();
          });
        });
        break;
      default:
        console.log("what???");
        break;
    }
  };

  const closeBuildForm = () => {
    buildForm.value = false;
    buildAction.value = "Add";
  };

  const buildForDeleteName = ref("");
  const buildForDeleteShortName = ref("");

  const buildError = ref("");

  const getAllBuilds = async () => {
    try {
      const resp = await axios.get<Build, AxiosResponse<Build[]>>(
        buildsEndpoint
      );
      return resp.data;
    } catch (err) {
      console.log(err);
      return [];
    }
  };

  const getBuild = async (b: string) => {
    try {
      const resp = await axios.get<Build, AxiosResponse<Build>>(
        buildEndpoint(b)
      );
      build.value = resp.data;
    } catch (err) {
      buildError.value = err;
    }
  };

  const addBuild = async (name: string, shortName: string) => {
    try {
      axios.post(buildsEndpoint, {
        name: name,
        shortName: shortName,
      });
    } catch (err) {
      buildError.value = err;
    }
  };

  const updateBuild = async (
    buildForUpdate: string,
    name: string,
    shortName: string
  ) => {
    try {
      axios.put(buildEndpoint(buildForUpdate), {
        name: name,
        shortName: shortName,
      });
    } catch (err) {
      buildError.value = err;
    }
  };

  const deleteBuild = async (build: string) => {
    try {
      axios.delete(buildEndpoint(build));
    } catch (err) {
      buildError.value = err;
    }
  };

  return {
    builds,

    buildForm,
    buildName,
    buildShortName,
    buildAction,

    openBuildForm,
    handleSubmitBuild,
    closeBuildForm,

    buildForDeleteName,
    buildForDeleteShortName,

    buildError,

    getAllBuilds,
    getBuild,
    addBuild,
    updateBuild,
    deleteBuild,
  };
}
