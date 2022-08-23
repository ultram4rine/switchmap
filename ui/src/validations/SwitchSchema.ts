import { object, boolean, string, number } from "yup";

const macRegexp = /^([0-9a-f]{2}[:-]?){5}[0-9a-f]{2}$/i;
const ipRegexp =
  /^((25[0-5]|2[0-4][0-9]|[1]?[0-9][0-9]?)(\.)){3}(25[0-5]|2[0-4][0-9]|[1]?[0-9][0-9]?)$/;

export const SwitchSchema = object({
  retrieveFromNetData: boolean().required(),
  retrieveIPFromDNS: boolean().required(),
  name: string().required(),
  ip: string().min(7).max(15).matches(ipRegexp).required(),
  mac: string().min(12).max(17).matches(macRegexp).required(),
  retrieveUpLinkFromSeens: boolean().required(),
  upSwitchName: string().required(),
  upLink: string().required(),
  retrieveTechDataFromSNMP: boolean().required(),
  revision: string().required(),
  serial: string().required(),
  snmpCommunity: string().required(),
  buildShortName: string().required(),
  floorNumber: number().required(),
  positionTop: number().required(),
  positionLeft: number().required(),
});
