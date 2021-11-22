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
  buildShortName: string;
  floorNumber: number;
  positionTop: number;
  positionLeft: number;
  upSwitchName: string;
  upLink: string;
};

export type SwitchResult = {
  sw: SwitchResponse;
  seen: boolean;
  snmp: boolean;
};

export type SavePositionRequest = {
  top: number;
  left: number;
};
