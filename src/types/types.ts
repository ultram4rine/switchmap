export interface Build {
  name: string;
  addr: string;
  floorNumber: number;
  switchNumber: number;
}

export interface Builds {
  builds: Array<Build>;
}

export interface Floor {
  build: Build;
  number: number;
  switchNumber: number;
}

export interface Switch {
  name: string;
}
