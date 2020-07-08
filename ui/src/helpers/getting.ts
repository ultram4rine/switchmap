import { Ref, ref } from "@vue/composition-api";
import axios, { AxiosResponse } from "axios";

import { Build, Floor, Switch } from "@/interfaces";
import { config } from "@/config";

const buildsEndpoint = `${config.apiURL}/builds`;
const buildEndpoint = `${config.apiURL}/build`;
const switchesEndpoint = `${config.apiURL}/switches`;

export function getAllBuilds() {
  let builds: Ref<Build[]> = ref([]);
  axios
    .get<Build, AxiosResponse<Build[]>>(buildsEndpoint)
    .then((resp) => (builds.value = resp.data))
    .catch((err) => console.log(err));

  return builds;
}

export function getBuild(shortName: string) {
  let build: Ref<Build | undefined> = ref();
  axios
    .get<Build, AxiosResponse<Build>>(`${buildEndpoint}/${shortName}`)
    .then((resp) => (build.value = resp.data))
    .catch((err) => console.log(err));

  return build;
}

export function getFloorsOf(buildShortName: string) {
  let floors: Ref<Floor[]> = ref([]);
  axios
    .get<Floor, AxiosResponse<Floor[]>>(
      `${buildEndpoint}/${buildShortName}/floors`
    )
    .then((resp) => (floors.value = resp.data))
    .catch((err) => console.log(err));

  return floors;
}

export function getAllSwitches() {
  let switches: Ref<Switch[]> = ref([]);
  axios
    .get<Switch, AxiosResponse<Switch[]>>(switchesEndpoint)
    .then((resp) => (switches.value = resp.data))
    .catch((err) => console.log(err));

  return switches;
}
