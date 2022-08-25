import type { AxiosResponse } from "axios";

import api from ".";

import type { BuildRequest, BuildResponse } from "@/interfaces/build";

export const getBuilds = async (): Promise<BuildResponse[]> => {
  const resp = await api.get<BuildResponse, AxiosResponse<BuildResponse[]>>(
    "/builds"
  );
  /* const resp = {
    data: [
      {
        name: "Build 1",
        shortName: "b1",
        floorsNumber: 2,
        switchesNumber: 10,
      },
      {
        name: "Build 2",
        shortName: "b2",
        floorsNumber: 2,
        switchesNumber: 10,
      },
    ],
  }; */
  return resp.data;
};

export const getBuild = async (shortName: string): Promise<BuildResponse> => {
  const resp = await api.get<BuildResponse, AxiosResponse<BuildResponse>>(
    `/builds/${shortName}`
  );
  return resp.data;
};

export const searchBuilds = async (s: string): Promise<BuildResponse[]> => {
  const builds = await getBuilds();
  s = s.toLowerCase();
  return builds.filter((b) => {
    return (
      b.name.toLowerCase().indexOf(s) !== -1 ||
      b.shortName.toLowerCase().indexOf(s) !== -1
    );
  });
};

export const addBuild = async (build: BuildRequest): Promise<void> => {
  await api.post("/builds", build);
};

export const editBuild = async (
  build: BuildRequest,
  oldShortName: string
): Promise<void> => {
  await api.put(`/builds/${oldShortName}`, build);
};

export const deleteBuild = async (shortName: string): Promise<void> => {
  await api.delete(`/builds/${shortName}`);
};
