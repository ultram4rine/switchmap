import { Ref, ref } from "@vue/composition-api";
import axios, { AxiosResponse } from "axios";

import { Build, Floor, Switch } from "@/interfaces";
import { config } from "@/config";

const buildsEndpoint = `${config.apiURL}/builds`;
const buildEndpoint = `${config.apiURL}/build`;
const switchesEndpoint = `${config.apiURL}/switches`;

export function getAllBuilds() {
  let builds: Build[] = [];
  axios
    .get<Build, AxiosResponse<Build[]>>(buildsEndpoint)
    .then((resp) => (builds = resp.data))
    .catch((err) => console.log(err));

  return builds;
}

export function getBuild(shortName: string) {
  let build = {} as Build;
  axios
    .get<Build, AxiosResponse<Build>>(`${buildEndpoint}/${shortName}`)
    .then((resp) => (build = resp.data))
    .catch((err) => console.log(err));

  return build;
}

export function getFloorsOf(buildShortName: string) {
  let floors: Floor[] = [];
  axios
    .get<Floor, AxiosResponse<Floor[]>>(
      `${buildEndpoint}/${buildShortName}/floors`
    )
    .then((resp) => (floors = resp.data))
    .catch((err) => console.log(err));

  return floors;
}

export function getAllSwitches() {
  let switches: Switch[] = [];
  axios
    .get<Switch, AxiosResponse<Switch[]>>(switchesEndpoint)
    .then((resp) => (switches = resp.data))
    .catch((err) => console.log(err));

  return switches;
}
