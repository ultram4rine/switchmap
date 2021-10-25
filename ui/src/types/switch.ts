export type SwitchRequest = SwitchResponse & {
  snmpCommunity: string;
  retrieveFromNetData: boolean;
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
