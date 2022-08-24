import { AxiosResponse } from "axios";

import api from ".";

import { FloorRequest, FloorResponse } from "@/interfaces/floor";

export const getFloorsOf = async (
  shortName: string
): Promise<FloorResponse[]> => {
  const resp = await api.get<FloorResponse, AxiosResponse<FloorResponse[]>>(
    `/builds/${shortName}/floors`
  );
  /* const resp = {
    data: [
      { number: 1, switchesNumber: 5 },
      { number: 2, switchesNumber: 5 },
    ],
  }; */
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
