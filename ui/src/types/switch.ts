export type SwitchRequest = SwitchResponse & {
  snmpCommunity: string;
  retrieveFromNetData: boolean;
  retrieveUpLinkFromSeens: boolean;
  retrieveTechDataFromSNMP: boolean;
  ipResolveMethod: string;
};

export type SwitchResponse = {
  name: string;
  ip: string;
  mac: string;
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
};

export type SavePositionRequest = {
  top: number;
  left: number;
};
