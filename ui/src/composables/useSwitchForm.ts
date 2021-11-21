import { ref, Ref } from "@vue/composition-api";

import { SwitchRequest } from "@/types/switch";
import { addSwitch, editSwitch } from "@/api/switches";

const useSwitchForm = (): {
  form: Ref<boolean>;
  formAction: Ref<string>;
  sw: Ref<SwitchRequest>;
  openForm: (
    action: "Add" | "Edit",
    swit?: SwitchRequest,
    build?: string,
    floor?: number
  ) => void;
  submitForm: (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    upSwitchName: string,
    upLink: string,
    snmpCommunity: string,
    revision: string,
    serial: string,
    build: string,
    floor: number,
    retrieveFromNetData: boolean,
    retrieveUpLinkFromSeens: boolean,
    retrieveTechDataFromSNMP: boolean,
    action: "Add" | "Edit"
  ) => Promise<void>;
  closeForm: () => void;
} => {
  const form = ref(false);
  const formAction = ref("");

  const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);
  const oldName = ref("");

  const openForm = (
    action: "Add" | "Edit",
    swit?: SwitchRequest,
    build?: string,
    floor?: number
  ): void => {
    if (swit) {
      oldName.value = swit.name;
      sw.value = swit;
      sw.value.ipResolveMethod = "Direct";
      sw.value.retrieveFromNetData = false;
      sw.value.retrieveUpLinkFromSeens = false;
      sw.value.retrieveTechDataFromSNMP = false;
    } else {
      sw.value = {
        mac: "",
        ipResolveMethod: "DNS",
        retrieveFromNetData: true,
        retrieveUpLinkFromSeens: true,
        retrieveTechDataFromSNMP: true,
        buildShortName: build ? build : null,
        floorNumber: floor ? floor : null,
      } as SwitchRequest;
    }

    formAction.value = action;
    form.value = true;
  };

  const submitForm = async (
    name: string,
    ipResolveMethod: string,
    ip: string,
    mac: string,
    upSwitchName: string,
    upLink: string,
    snmpCommunity: string,
    revision: string,
    serial: string,
    build: string,
    floor: number,
    retrieveFromNetData: boolean,
    retrieveUpLinkFromSeens: boolean,
    retrieveTechDataFromSNMP: boolean,
    action: "Add" | "Edit"
  ): Promise<void> => {
    switch (action) {
      case "Add": {
        await addSwitch({
          snmpCommunity,
          retrieveFromNetData,
          retrieveUpLinkFromSeens,
          retrieveTechDataFromSNMP,
          ipResolveMethod,
          name,
          ip,
          mac,
          upSwitchName,
          upLink,
          buildShortName: build,
          floorNumber: floor,
          revision,
          serial,
        } as SwitchRequest);
        closeForm();
        break;
      }
      case "Edit": {
        await editSwitch(
          {
            snmpCommunity,
            retrieveFromNetData,
            retrieveUpLinkFromSeens,
            retrieveTechDataFromSNMP,
            ipResolveMethod,
            name,
            ip,
            mac,
            upSwitchName,
            upLink,
            buildShortName: build,
            floorNumber: floor,
            positionTop: sw.value.positionTop,
            positionLeft: sw.value.positionLeft,
            revision,
            serial,
          } as SwitchRequest,
          oldName.value
        );
        closeForm();
        break;
      }
      default:
        break;
    }
  };

  const closeForm = (): void => {
    form.value = false;
    sw.value = {} as SwitchRequest;
    oldName.value = "";
    formAction.value = "";
  };

  return {
    form,
    formAction,

    sw,

    openForm,
    submitForm,
    closeForm,
  };
};

export default useSwitchForm;
