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
  const build: Ref<Build> = ref({} as Build);

  const buildForm = ref(false);
  const buildName = ref("");
  const buildShortName = ref("");

  const buildForDeleteName = ref("");
  const buildForDeleteShortName = ref("");

  const buildError = ref("");

  const getAllBuilds = async () => {
    try {
      const resp = await axios.get<Build, AxiosResponse<Build[]>>(
        buildsEndpoint
      );
      builds.value = resp.data;
    } catch (err) {
      buildError.value = err;
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
    build,

    buildForm,
    buildName,
    buildShortName,

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
