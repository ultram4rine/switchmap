export interface BuildRequest {
  name: string;
  shortName: string;
}

export interface BuildResponse extends BuildRequest {
  floorsNumber: number;
  switchesNumber: number;
}
