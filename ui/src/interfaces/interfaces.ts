export interface Build {
  name: string;
  shortName: string;
  floorsNumber: number;
  switchesNumber: number;
}

export interface Floor {
  number: number;
  switchesNumber: number;
}

export interface Switch {
  name: string;
  ip: string;
  mac: string;
  snmpCommunity: string;
  revision: string;
  serial: string;
  portsNumber: number;
  buildShortName: string;
  floorNumber: number;
  positionTop: number;
  positionLeft: number;
  upSwitchName: string;
  upSwitchMAC: string;
  upLink: string;
}
