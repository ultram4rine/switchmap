import "@testing-library/jest-dom";
import VueCompositionAPI from "@vue/composition-api";

import BuildForm from "@/components/forms/BuildForm.vue";
import { BuildRequest } from "@/interfaces/build";

import { renderWithVuetify } from "../../../common";

describe("BuildForm", () => {
  it("makes sure the build form is rendered without value", () => {
    const build = {} as BuildRequest;

    const { getByText } = renderWithVuetify(
      BuildForm,
      {
        props: { form: true, action: "Add", build },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText("New build")).toBeInTheDocument();
    expect(getByText("Add")).toBeInTheDocument();
  });

  it("makes sure the build form is rendered with value", () => {
    const build = {
      name: "Build 1",
      shortName: "b1",
    } as BuildRequest;

    const { getByText } = renderWithVuetify(
      BuildForm,
      {
        props: { form: true, action: "Edit", build },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText("Change build")).toBeInTheDocument();
    expect(getByText("Edit")).toBeInTheDocument();
  });
});
