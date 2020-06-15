export interface Build {
  name: string;
  addr: string;
  floorsNumber: number;
  switchesNumber: number;
}

export interface Floor {
  build: Build;
  number: number;
  switchNumber: number;
}

export interface Switch {
  name: string;
  upSwitch: string;
  port: string;
}
