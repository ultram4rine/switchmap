export type BuildRequest = {
  name: string;
  shortName: string;
};

export type BuildResponse = BuildRequest & {
  floorsNumber: number;
  switchesNumber: number;
};
