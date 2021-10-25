import { AxiosResponse } from "axios";

import api from ".";

import { SwitchRequest, SwitchResponse } from "@/types/switch";
import { macDenormalization } from "@/helpers";

export const getSNMPCommunities = async (): Promise<string[]> => {
  const resp = await api.get<string, AxiosResponse<string[]>>(
    "/switches/snmp/communities"
  );
  return resp.data;
};

export const getSwitches = async (): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    "/switches"
  );
  return resp.data;
};

export const getSwitchesOfBuild = async (
  shortName: string
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/switches`
  );
  resp.data.forEach((sw) => (sw.mac = macDenormalization(sw.mac)));
  return resp.data;
};

export const getSwitchesOfFloor = async (
  shortName: string,
  number: number
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/floors/${number}/switches`
  );
  resp.data.forEach((sw) => (sw.mac = macDenormalization(sw.mac)));
  return resp.data;
};

export const getSwitch = async (name: string): Promise<SwitchResponse> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse>>(
    `/switches/${name}`
  );
  resp.data.mac = macDenormalization(resp.data.mac);
  return resp.data;
};

export const addSwitch = async (sw: SwitchRequest): Promise<void> => {
  await api.post("/switches", sw);
};

export const editSwitch = async (sw: SwitchRequest): Promise<void> => {
  await api.put(`/switches/${sw.name}`, sw);
};

export const deleteSwitch = async (name: string): Promise<void> => {
  await api.delete(`/switches/${name}`);
};
