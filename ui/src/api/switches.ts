import type { AxiosResponse } from "axios";

import api from ".";

import type {
  SavePositionRequest,
  SwitchRequest,
  SwitchResponse,
  SwitchResult,
} from "@/interfaces/switch";

export const syncSwitches = async (): Promise<string> => {
  const resp = await api.get<string, AxiosResponse<string>>("/switches/sync");
  return resp.data;
};

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

/**
 * @deprecated
 */
export const getSwitchesOfFloor = async (
  shortName: string,
  number: number
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/floors/${number}/switches`
  );
  return resp.data;
};

export const getUnplacedSwitchesOfBuild = async (
  shortName: string
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/switches/unplaced`
  );
  return resp.data;
};

export const getPlacedSwitchesOfFloor = async (
  shortName: string,
  number: number
): Promise<SwitchResponse[]> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse[]>>(
    `/builds/${shortName}/floors/${number}/switches/placed`
  );
  return resp.data;
};

export const getSwitch = async (name: string): Promise<SwitchResponse> => {
  const resp = await api.get<SwitchResponse, AxiosResponse<SwitchResponse>>(
    `/switches/${name}`
  );
  return resp.data;
};

export const addSwitch = async (sw: SwitchRequest): Promise<SwitchResult> => {
  const resp = await api.post<SwitchResult, AxiosResponse<SwitchResult>>(
    "/switches",
    sw
  );
  return resp.data;
};

export const editSwitch = async (
  sw: SwitchRequest,
  oldName: string
): Promise<SwitchResult> => {
  const resp = await api.put<SwitchResult, AxiosResponse<SwitchResult>>(
    `/switches/${oldName}`,
    sw
  );
  return resp.data;
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
