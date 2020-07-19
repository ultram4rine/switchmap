import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Build } from "@/interfaces";

const buildsEndpoint = `${config.apiURL}/builds`;
const buildEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}`;
};

export default function() {
  const builds: Ref<Build[]> = ref([]);

  const buildForm = ref(false);
  const buildAction = ref("Add");
  const buildName = ref("");
  const buildShortName = ref("");

  /*
   * FU = ForUpdate;
   * FD = ForDelete;
   * N = Name;
   * SN = ShortName;
   */
  const buildFUSN = ref("");
  const buildFDN = ref("");
  const buildFDSN = ref("");

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
      return resp.data;
    } catch (err) {
      console.log(err);
      return {} as Build;
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

  const openBuildForm = (action: string, b?: Build) => {
    buildAction.value = action;
    switch (action) {
      case "Add":
        buildName.value = "";
        buildShortName.value = "";
        break;
      case "Change":
        if (b != undefined) {
          buildFUSN.value = b.shortName;

          buildName.value = b.name;
          buildShortName.value = b.shortName;
        }
        break;
      default:
        break;
    }
    buildForm.value = true;
  };

  const closeBuildForm = () => {
    buildForm.value = false;
    buildAction.value = "Add";

    buildName.value = "";
    buildShortName.value = "";
  };

  const handleSubmitBuild = (name: string, shortName: string) => {
    buildName.value = name;
    buildShortName.value = shortName;
    switch (buildAction.value) {
      case "Add":
        addBuild(name, shortName).then(() =>
          getAllBuilds().then(bs => {
            builds.value = bs;
            closeBuildForm();
          })
        );
        break;
      case "Change":
        updateBuild(buildFUSN.value, name, shortName).then(() => {
          getBuild(shortName).then(build => {
            const i = builds.value.findIndex(
              b => b.shortName == build.shortName
            );
            builds.value[i] = build;

            closeBuildForm();
          });
        });
        break;
      default:
        console.log("what???");
        break;
    }
  };

  return {
    builds,

    buildForm,
    buildAction,
    buildName,
    buildShortName,

    buildFUSN,
    buildFDN,
    buildFDSN,

    buildError,

    getAllBuilds,
    getBuild,
    addBuild,
    updateBuild,
    deleteBuild,

    openBuildForm,
    closeBuildForm,
    handleSubmitBuild,
  };
}
