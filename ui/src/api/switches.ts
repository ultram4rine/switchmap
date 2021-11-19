import { AxiosResponse } from "axios";

import api from ".";

import {
  SavePositionRequest,
  SwitchRequest,
  SwitchResponse,
} from "@/types/switch";

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
  return resp.data;
};

export const getSwitchesOfFloor = async (
  shortName: string,
  number: number
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/floors/${number}/switches`
  );
  return resp.data;
};

export const getSwitch = async (name: string): Promise<SwitchResponse> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse>>(
    `/switches/${name}`
  );
  return resp.data;
};

export const addSwitch = async (sw: SwitchRequest): Promise<void> => {
  await api.post("/switches", sw);
};

export const editSwitch = async (
  sw: SwitchRequest,
  oldName: string
): Promise<void> => {
  await api.put(`/switches/${oldName}`, sw);
};

export const updatePosition = async (
  name: string,
  position: SavePositionRequest
): Promise<void> => {
  await api.patch(`/switches/${name}`, position);
};

export const deleteSwitch = async (name: string): Promise<void> => {
  await api.delete(`/switches/${name}`);
};
