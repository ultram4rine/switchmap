import { AxiosResponse } from "axios";

import api from ".";

import { Switch } from "@/types/switch";

export const getSNMPCommunities = async (): Promise<string[]> => {
  const resp = await api.get<string, AxiosResponse<string[]>>(
    "/switches/snmp/communities"
  );
  return resp.data;
};

export const getSwitches = async (): Promise<Switch[]> => {
  const resp = await api.get<Switch, AxiosResponse<Switch[]>>("/switches");
  return resp.data;
};

export const getSwitchesOfBuild = async (
  shortName: string
): Promise<Switch[]> => {
  const resp = await api.get<Switch, AxiosResponse<Switch[]>>(
    `/builds/${shortName}/switches`
  );
  return resp.data;
};

export const getSwitchesOfFloor = async (
  shortName: string,
  number: number
): Promise<Switch[]> => {
  const resp = await api.get<Switch, AxiosResponse<Switch[]>>(
    `/builds/${shortName}/floors/${number}/switches`
  );
  return resp.data;
};

export const getSwitch = async (name: string): Promise<Switch> => {
  const resp = await api.get<Switch, AxiosResponse<Switch>>(
    `/switches/${name}`
  );
  return resp.data;
};

export const addSwitch = async (sw: Switch): Promise<void> => {
  await api.post("/switches", sw);
};

export const editSwitch = async (sw: Switch): Promise<void> => {
  await api.put(`/switches/${sw.name}`, sw);
};

export const deleteSwitch = async (name: string): Promise<void> => {
  await api.delete(`/switches/${name}`);
};
