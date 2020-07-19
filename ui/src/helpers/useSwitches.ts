import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Build, Floor, Switch } from "@/interfaces";

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

  const switchForm = ref(false);
  const switchAction = ref("Add");
  const switchName = ref("");
  const switchIPResolveMethod = ref("");
  const switchIP = ref("");
  const switchMAC = ref("");
  const switchSNMPCommunity = ref("");
  const switchBuild = ref("");
  const switchFloor = ref("");

  const switchError = ref("");

  const getAllSwitches = async () => {
    try {
      const resp = await axios.get<Switch, AxiosResponse<Switch[]>>(
        switchesEndpoint
      );
      return resp.data;
    } catch (err) {
      console.log(err);
      return [];
    }
  };

  const getSwitchesOf = async (b: string, f: string) => {
    try {
      const resp = await axios.get<Switch, AxiosResponse<Switch[]>>(
        switchesOfFloorEndpoint(b, f)
      );
      return resp.data;
    } catch (err) {
      console.log(err);
      return [];
    }
  };

  const addSwitch = async (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    snmpCommunity: string,
    b?: string,
    f?: string
  ) => {
    const endpoint =
      b != undefined && f != undefined
        ? switchesOfFloorEndpoint(b, f)
        : switchesEndpoint;

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

  const openSwitchForm = (
    action: string,
    b?: Build,
    f?: Floor,
    sw?: Switch
  ) => {
    switchAction.value = action;
    switch (action) {
      case "Add":
        switchName.value = "";
        switchIPResolveMethod.value = "Direct";
        switchIP.value = "";
        switchMAC.value = "";
        switchSNMPCommunity.value = "Public";
        if (b != undefined) {
          switchBuild.value = b.shortName;
        } else {
          switchBuild.value = "";
        }
        if (f != undefined) {
          switchFloor.value = f.number.toString();
        } else {
          switchFloor.value = "";
        }
        break;
      case "Change":
        console.log("later");
        break;
      default:
        break;
    }
    switchForm.value = true;
  };

  const closeSwitchForm = () => {
    switchForm.value = false;

    switchName.value = "";
    switchIPResolveMethod.value = "Direct";
    switchIP.value = "";
    switchMAC.value = "";
    switchSNMPCommunity.value = "Public";
    switchBuild.value = "";
    switchFloor.value = "";

    switchAction.value = "Add";
  };

  const handleSubmitSwitchFromSwitchesView = (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    snmpCommunity: string,
    b?: string,
    f?: string
  ) => {
    switchName.value = name;
    switchIPResolveMethod.value = ipResolveMethod;
    switchIP.value = ip;
    switchMAC.value = mac;
    switchSNMPCommunity.value = snmpCommunity;

    addSwitch(name, ipResolveMethod, ip, mac, snmpCommunity, b, f).then(() =>
      getAllSwitches().then((sws) => {
        switches.value = sws;
        closeSwitchForm();
      })
    );
  };

  const handleSubmitSwitchFromFloorView = (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    snmpCommunity: string,
    b: string,
    f: string
  ) => {
    switchName.value = name;
    switchIPResolveMethod.value = ipResolveMethod;
    switchIP.value = ip;
    switchMAC.value = mac;
    switchSNMPCommunity.value = snmpCommunity;

    addSwitch(name, ipResolveMethod, ip, mac, snmpCommunity, b, f).then(() =>
      getSwitchesOf(b, f).then((sws) => {
        switches.value = sws;
        closeSwitchForm();
      })
    );
  };

  return {
    switches,

    switchForm,
    switchAction,
    switchName,
    switchIPResolveMethod,
    switchIP,
    switchMAC,
    switchSNMPCommunity,
    switchBuild,
    switchFloor,

    switchError,

    getAllSwitches,
    getSwitchesOf,
    addSwitch,

    openSwitchForm,
    closeSwitchForm,
    handleSubmitSwitchFromSwitchesView,
    handleSubmitSwitchFromFloorView,
  };
}
