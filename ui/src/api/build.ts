import axios, { AxiosResponse } from "axios";

import { config } from "@/config";

import { Build } from "@/interfaces";

const buildsEndpoint = `${config.apiURL}/builds`;
const buildEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}`;
};

const getAllBuilds = () => {
  let builds: Build[] = [];
  axios
    .get<Build, AxiosResponse<Build[]>>(buildsEndpoint)
    .then((resp) => (builds = resp.data))
    .catch((err) => console.log(err));

  return builds;
};

const getBuild = (build: string) => {
  let b = {} as Build;
  axios
    .get<Build, AxiosResponse<Build>>(buildEndpoint(build))
    .then((resp) => (b = resp.data))
    .catch((err) => console.log(err));

  return b;
};

const addBuild = (name: string, shortName: string) => {
  axios
    .post(buildsEndpoint, {
      name: name,
      shortName: shortName,
    })
    .catch((err) => console.log(err));
};

const updateBuild = (
  buildForUpdate: string,
  name: string,
  shortName: string
) => {
  axios
    .put(buildEndpoint(buildForUpdate), {
      name: name,
      shortName: shortName,
    })
    .catch((err) => console.log(err));
};

const deleteBuild = (build: string) => {
  axios.delete(buildEndpoint(build)).catch((err) => console.log(err));
};

export { getAllBuilds, getBuild, addBuild, updateBuild, deleteBuild };
