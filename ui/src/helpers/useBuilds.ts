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
  const buildName = ref("");
  const buildShortName = ref("");

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

  return {
    builds,
    buildForm,
    buildName,
    buildShortName,
    buildError,
    getAllBuilds,
  };
}
