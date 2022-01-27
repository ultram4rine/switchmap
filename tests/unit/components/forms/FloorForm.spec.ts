import "@testing-library/jest-dom";
import VueCompositionAPI from "@vue/composition-api";

import FloorForm from "@/components/forms/FloorForm.vue";
import { FloorRequest } from "@/interfaces/floor";

import { renderWithVuetify } from "../../../common";

describe("FloorForm", () => {
  it("makes sure the floor form is rendered", () => {
    const floor = {} as FloorRequest;

    const { getByText } = renderWithVuetify(
      FloorForm,
      {
        props: { form: true, floor },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText("New floor")).toBeInTheDocument();
    expect(getByText("Add")).toBeInTheDocument();
  });
});
