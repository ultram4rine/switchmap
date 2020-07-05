export interface Build {
  name: string;
  shortName: string;
  floorsNumber: number;
  switchesNumber: number;
}

export interface Floor {
  number: number;
  switchNumber: number;
}

export interface Switch {
  name: string;
  ip: string;
  mac: string;
  vendor: string;
  revision: string;
  serial: string;
  upSwitch: string;
  port: string;
  posTop: number;
  posLeft: number;
  buildShortName: string;
  floorNumber: number;
}
