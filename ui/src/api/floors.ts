import { AxiosResponse } from "axios";

import api from ".";

import { FloorRequest, FloorResponse } from "@/types/floor";

export const getFloorsOf = async (
  shortName: string
): Promise<FloorResponse[]> => {
  const resp = await api.get<FloorResponse, AxiosResponse<FloorResponse[]>>(
    `/builds/${shortName}/floors`
  );
  return resp.data;
};

export const getFloor = async (
  shortName: string,
  number: number
): Promise<FloorResponse> => {
  const resp = await api.get<FloorResponse, AxiosResponse<FloorResponse>>(
    `/builds/${shortName}/floors/${number}`
  );
  return resp.data;
};

export const addFloor = async (floor: FloorRequest): Promise<void> => {
  await api.post(`/builds/${floor.buildShortName}`, floor);
};

export const deleteFloor = async (
  shortName: string,
  number: number
): Promise<void> => {
  await api.delete(`/builds/${shortName}/floors/${number}`);
};
