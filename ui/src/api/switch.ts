import axios, { AxiosResponse } from "axios";

import { config } from "@/config";

import { Switch } from "@/interfaces";

const switchesEndpoint = `${config.apiURL}/switches`;
const switchEndpoint = (sw: string) => {
  return `${config.apiURL}/switches/${sw}`;
};
const switchesOfBuildEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}/switches`;
};
const switchesOfFloorEndpoint = (build: string, floor: string) => {
  return `${config.apiURL}/builds/${build}/${floor}/switches`;
};

const getAllSwitches = () => {
  let switches: Switch[] = [];
  axios
    .get<Switch, AxiosResponse<Switch[]>>(switchesEndpoint)
    .then((resp) => (switches = resp.data))
    .catch((err) => console.log(err));
};

const getSwitchesOf = (build: string, floor: string) => {
  let switches: Switch[] = [];
  axios
    .get<Switch, AxiosResponse<Switch[]>>(switchesOfFloorEndpoint(build, floor))
    .then((resp) => (switches = resp.data))
    .catch((err) => console.log(err));
};

const addSwitch = (
  name: string,
  ipResolveMethod: string,
  ip: string,
  mac: string,
  snmpCommunity: string,
  build: string,
  floor: string
) => {
  let endpoint =
    build !== "" && floor !== ""
      ? switchesOfFloorEndpoint(build, floor)
      : switchesEndpoint;

  axios
    .post(endpoint, {
      name: name,
      ipResolveMethod: ipResolveMethod,
      ip: ip,
      mac: mac,
      snmpCommunity: snmpCommunity,
    })
    .catch((err) => console.log(err));
};

export { getAllSwitches, getSwitchesOf, addSwitch };
