import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Switch } from "@/interfaces";

const switchesEndpoint = `${config.apiURL}/switches`;
const switchEndpoint = (sw: string) => {
  return `${config.apiURL}/switches/${sw}`;
};
const switchesOfBuildEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}/switches`;
};
const switchesOfFloorEndpoint = (build: string, floor: string) => {
  return `${config.apiURL}/builds/${build}/${floor}/switches`;
};

export default function () {
  const switches: Ref<Switch[]> = ref([]);
  const sw: Ref<Switch> = ref({} as Switch);

  const switchForm = ref(false);
  const switchName = ref("");
  const switchIPResolveMethod = ref("");
  const switchIP = ref("");
  const switchMAC = ref("");
  const switchSNMPCommunity = ref("");
  const switchBuild = ref("");
  const switchFloor = ref("");

  const closeSwitchForm = () => {
    switchForm.value = false;
    switchName.value = "";
    switchIPResolveMethod.value = "";
    switchIP.value = "";
    switchMAC.value = "";
    switchSNMPCommunity.value = "";
    switchBuild.value = "";
    switchFloor.value = "";
  };

  const switchError = ref("");

  const getAllSwitches = async () => {
    try {
      const resp = await axios.get<Switch, AxiosResponse<Switch[]>>(
        switchesEndpoint
      );
      switches.value = resp.data;
    } catch (err) {
      switchError.value = err;
    }
  };

  const getSwitchesOf = async (b: string, f: string) => {
    try {
      const resp = await axios.get<Switch, AxiosResponse<Switch[]>>(
        switchesOfFloorEndpoint(b, f)
      );
      switches.value = resp.data;
    } catch (err) {
      switchError.value = err;
    }
  };

  const addSwitch = async (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    snmpCommunity: string,
    b: string,
    f: string
  ) => {
    const endpoint =
      b !== "" && f !== "" ? switchesOfFloorEndpoint(b, f) : switchesEndpoint;

    try {
      axios.post(endpoint, {
        name: name,
        ipResolveMethod: ipResolveMethod,
        ip: ip,
        mac: mac,
        snmpCommunity: snmpCommunity,
      });
    } catch (err) {
      switchError.value = err;
    }
  };

  return {
    switches,
    sw,

    switchForm,
    switchName,
    switchIPResolveMethod,
    switchIP,
    switchMAC,
    switchSNMPCommunity,
    switchBuild,
    switchFloor,

    closeSwitchForm,

    switchError,

    getAllSwitches,
    getSwitchesOf,
    addSwitch,
  };
}
