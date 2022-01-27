import "@testing-library/jest-dom";
import VueCompositionAPI from "@vue/composition-api";

import MockAdapter from "axios-mock-adapter";

import api from "@/api";

import SwitchForm from "@/components/forms/SwitchForm.vue";
import { SwitchRequest, SwitchResponse } from "@/interfaces/switch";

import { renderWithVuetify } from "../../../common";

const mock = new MockAdapter(api);

mock.onGet("/switches").reply(200, [
  {
    name: "switch0",
    ip: "192.168.1.3",
    mac: "000000000001",
    revision: "Model A",
    serial: "012346",
    buildShortName: "b1",
    floorNumber: 1,
    positionTop: 0,
    positionLeft: 0,
    upSwitchName: "",
    upLink: "",
  },
  {
    name: "switch2",
    ip: "192.168.1.4",
    mac: "000000000002",
    revision: "Model A",
    serial: "012347",
    buildShortName: "b1",
    floorNumber: 1,
    positionTop: 0,
    positionLeft: 0,
    upSwitchName: "",
    upLink: "",
  },
] as SwitchResponse[]);
mock.onGet("/switches/snmp/communities").reply(200, ["public", "private"]);

describe("SwitchForm", () => {
  it("makes sure the switch form is rendered without value", () => {
    const swit = {} as SwitchRequest;

    const { getByText } = renderWithVuetify(
      SwitchForm,
      {
        props: { form: true, action: "Add", swit, needLocationFields: false },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText("New switch")).toBeInTheDocument();
    expect(getByText("Add")).toBeInTheDocument();
  });

  it("makes sure the switch form is rendered with value", () => {
    const swit = {
      name: "switch1",
      ip: "192.168.1.1",
      mac: "000000000000",
      revision: "Model A",
      serial: "012345",
      buildShortName: "b1",
      floorNumber: 1,
      positionTop: 0,
      positionLeft: 0,
      upSwitchName: "",
      upLink: "",
      snmpCommunity: "private",
      retrieveFromNetData: false,
      retrieveIPFromDNS: false,
      retrieveUpLinkFromSeens: false,
      retrieveTechDataFromSNMP: false,
    } as SwitchRequest;

    const { getByText } = renderWithVuetify(
      SwitchForm,
      {
        props: { form: true, action: "Edit", swit, needLocationFields: false },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText("Change switch")).toBeInTheDocument();
    expect(getByText("Edit")).toBeInTheDocument();
  });
});
