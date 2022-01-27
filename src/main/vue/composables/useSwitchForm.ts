import { ref, Ref } from "@vue/composition-api";

import { SwitchRequest, SwitchResult } from "@/interfaces/switch";
import { addSwitch, editSwitch } from "@/api/switches";

import { macNormalization } from "@/helpers";

export const useSwitchForm = (): {
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
    swit: SwitchRequest,
    action: "Add" | "Edit"
  ) => Promise<SwitchResult>;
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
      sw.value.retrieveFromNetData = false;
      sw.value.retrieveIPFromDNS = false;
      sw.value.retrieveUpLinkFromSeens = false;
      sw.value.retrieveTechDataFromSNMP = false;
    } else {
      sw.value = {
        mac: "",
        retrieveFromNetData: true,
        retrieveIPFromDNS: true,
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
    swit: SwitchRequest,
    action: "Add" | "Edit"
  ): Promise<SwitchResult> => {
    let sr: SwitchResult = {} as SwitchResult;
    swit.mac = swit.mac.length === 12 ? swit.mac : macNormalization(swit.mac);
    switch (action) {
      case "Add": {
        sr = await addSwitch(swit);
        closeForm();
        break;
      }
      case "Edit": {
        swit.positionTop = sw.value.positionTop;
        swit.positionLeft = sw.value.positionLeft;
        sr = await editSwitch(swit, oldName.value);
        closeForm();
        break;
      }
      default:
        break;
    }
    return sr;
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
