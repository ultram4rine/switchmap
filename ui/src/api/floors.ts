import { AxiosResponse } from "axios";

import api from ".";

import { Floor } from "@/types/floor";

export const getFloorsOf = async (shortName: string): Promise<Floor[]> => {
  const resp = await api.get<Floor, AxiosResponse<Floor[]>>(
    `/builds/${shortName}/floors`
  );
  return resp.data;
};

export const getFloor = async (
  shortName: string,
  number: number
): Promise<Floor> => {
  const resp = await api.get<Floor, AxiosResponse<Floor>>(
    `/builds/${shortName}/floors/${number}`
  );
  return resp.data;
};

export const addFloor = async (
  name: string,
  shortName: string,
  number: number
): Promise<void> => {
  await api.post(`/builds/${shortName}`, {
    number,
    buildName: name,
    buildShortName: shortName,
  });
};

export const deleteFloor = async (
  shortName: string,
  number: number
): Promise<void> => {
  await api.delete(`/builds/${shortName}/floors/${number}`);
};
