import { AxiosResponse } from "axios";

import api from ".";

import { Build } from "@/types/build";

export const getBuilds = async (): Promise<Build[]> => {
  const resp = await api.get<Build, AxiosResponse<Build[]>>("/builds");
  return resp.data;
};

export const getBuild = async (shortName: string): Promise<Build> => {
  const resp = await api.get<Build, AxiosResponse<Build>>(
    `/builds/${shortName}`
  );
  return resp.data;
};

export const searchBuilds = async (s: string): Promise<Build[]> => {
  const builds = await getBuilds();
  s = s.toLowerCase();
  return builds.filter((b) => {
    return (
      b.name.toLowerCase().indexOf(s) !== -1 ||
      b.shortName.toLowerCase().indexOf(s) !== -1
    );
  });
};

export const addBuild = async (
  name: string,
  shortName: string
): Promise<void> => {
  await api.post("/builds", {
    name,
    shortName,
  });
};

export const editBuild = async (
  name: string,
  shortName: string,
  oldShortName: string
): Promise<void> => {
  await api.put(`/builds/${oldShortName}`, {
    name,
    shortName,
  });
};

export const deleteBuild = async (shortName: string): Promise<void> => {
  await api.delete(`/builds/${shortName}`);
};
