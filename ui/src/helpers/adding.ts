import axios from "axios";

import { config } from "@/config";

const addBuildEndpoint = `${config.apiURL}/build`;
const addFloorEndpoint = `${config.apiURL}/floor`;
const addSwitchEndpoint = `${config.apiURL}/switch`;

export function addBuild(name: string, shortName: string) {
  axios
    .post(addBuildEndpoint, {
      name: name,
      shortName: shortName,
    })
    .catch((err) => console.log(err));
}

export function addFloor(
  number: number,
  buildName: string,
  buildShortName: string
) {
  axios
    .post(addFloorEndpoint, {
      number: number,
      buildName: buildName,
      buildShortName: buildShortName,
    })
    .catch((err) => console.log(err));
}

export function addSwitch(
  name: string,
  mac: string,
  ipResolveMethod: string,
  ip: string,
  snmpCommunityType: string,
  snmpCommunity: string,
  build: string,
  floor: number
) {
  axios
    .post(addSwitchEndpoint, {
      name: name,
      mac: mac,
      ipResolveMethod: ipResolveMethod,
      ip: ip,
      snmpCommunityType: snmpCommunityType,
      snmpCommunity: snmpCommunity,
      build: build,
      floor: floor,
    })
    .catch((err) => console.log(err));
}
