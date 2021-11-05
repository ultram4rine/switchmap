import { ref, Ref } from "@vue/composition-api";

import { SwitchRequest } from "@/types/switch";
import { addSwitch, editSwitch } from "@/api/switches";

// TODO: fix build and floor getting from context.
const useSwitchForm = (): {
  form: Ref<boolean>;
  formAction: Ref<string>;
  sw: Ref<SwitchRequest>;
  oldSwitchName: Ref<string>;
  openForm: (action: "Add" | "Edit", swit?: SwitchRequest) => void;
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
    action: "Add" | "Edit",
    callback: () => void
  ) => void;
  closeForm: () => void;
} => {
  const form = ref(false);
  const formAction = ref("");

  const sw: Ref<SwitchRequest> = ref({} as SwitchRequest);
  const oldSwitchName = ref("");

  const openForm = (action: "Add" | "Edit", swit?: SwitchRequest): void => {
    if (swit) {
      sw.value = swit;
      sw.value.ipResolveMethod = "Direct";
      sw.value.retrieveFromNetData = false;
      sw.value.retrieveUpLinkFromSeens = false;
      sw.value.retrieveTechDataFromSNMP = false;
    } else {
      sw.value = {
        retrieveFromNetData: true,
        retrieveUpLinkFromSeens: true,
        retrieveTechDataFromSNMP: true,
      } as SwitchRequest;
    }

    formAction.value = action;
    form.value = true;
  };

  const submitForm = (
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
    action: "Add" | "Edit",
    callback: () => void
  ): void => {
    try {
      switch (action) {
        case "Add": {
          addSwitch({
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
          } as SwitchRequest).then(() => callback());
          closeForm();
          break;
        }
        case "Edit": {
          editSwitch({
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
          } as SwitchRequest).then(() => callback());
          closeForm();
          break;
        }
        default:
          break;
      }
    } catch (error: any) {
      console.log(error);
    }
  };

  const closeForm = (): void => {
    sw.value = {} as SwitchRequest;
    oldSwitchName.value = "";
    formAction.value = "";
    form.value = false;
  };

  return {
    form,
    formAction,

    sw,
    oldSwitchName,

    openForm,
    submitForm,
    closeForm,
  };
};

export default useSwitchForm;
