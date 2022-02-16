import "@testing-library/jest-dom";
import VueCompositionAPI from "@vue/composition-api";

import { routes } from "@/router";
import FloorCard from "@/components/cards/FloorCard.vue";
import { FloorResponse } from "~/interfaces/floor";

import { renderWithVuetify } from "../../../common";

describe("FloorCard", () => {
  it("makes sure the floor card is rendered", () => {
    const floor = {
      number: 1,
      switchesNumber: 2,
    } as FloorResponse;

    const { getByText } = renderWithVuetify(
      FloorCard,
      {
        routes,
        props: { shortName: "b1", floor },
      },
      (vue: any) => vue.use(VueCompositionAPI)
    );

    expect(getByText(`Floor ${floor.number.toString()}`)).toBeInTheDocument();
    expect(
      getByText(`${floor.switchesNumber.toString()} switches`)
    ).toBeInTheDocument();
  });
});
