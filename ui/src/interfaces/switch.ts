export interface SwitchRequest extends SwitchResponse {
  snmpCommunity: string;
  retrieveFromNetData: boolean;
  retrieveIPFromDNS: boolean;
  retrieveUpLinkFromSeens: boolean;
  retrieveTechDataFromSNMP: boolean;
}

export interface SwitchResponse {
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
}

export interface SwitchResult {
  sw: SwitchResponse;
  seen: boolean;
  snmp: boolean;
}

export interface SavePositionRequest {
  top: number;
  left: number;
}

export interface SwitchPosition {
  name: string;
  top: number;
  left: number;
  moving: boolean;
}
